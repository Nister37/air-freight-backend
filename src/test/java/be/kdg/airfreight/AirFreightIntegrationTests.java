package be.kdg.airfreight;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.events.LoadFinalisedEvent;
import be.kdg.airfreight.flightoperations.FlightNotFoundException;
import be.kdg.airfreight.flightoperations.FlightOperationsApi;
import be.kdg.airfreight.operationscoordination.LoadPlanningUnavailableException;
import be.kdg.airfreight.operationscoordination.port.in.command.ScheduleFeedCommand;
import be.kdg.airfreight.operationscoordination.port.in.usecase.GetPolicyUseCase;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RouteCargoFeedUseCase;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RouteScheduleFeedUseCase;
import be.kdg.airfreight.operationscoordination.port.in.usecase.UpdatePolicyUseCase;
import be.kdg.airfreight.partnermessaging.port.out.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.events.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AirFreightIntegrationTests {
    private static final LocalDateTime NOW = LocalDateTime.now(ZoneOffset.UTC);
    private static final LocalDate DATE = NOW.toLocalDate().minusDays(1);
    private static final AtomicInteger NUMBER = new AtomicInteger(100);
    @Autowired FlightOperationsApi flights;
    @Autowired RouteScheduleFeedUseCase schedules;
    @Autowired RouteCargoFeedUseCase cargo;
    @Autowired GetPolicyUseCase getPolicy;
    @Autowired UpdatePolicyUseCase updatePolicy;
    @Autowired ApplicationEventPublisher events;
    @Autowired PlatformTransactionManager transactions;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;
    @Autowired FailedEventPublications failedPublications;
    @MockitoBean RabbitTemplate rabbit;
    @MockitoSpyBean MessageStorePort messageStore;

    @BeforeEach
    void resetPolicy() {
        jdbc.update("delete from oc_policy");
    }

    @AfterEach
    void waitForCommittedListenersBeforeResettingMocks() {
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertThat(jdbc.queryForObject(
                "select count(*) from event_publication", Long.class)).isZero());
    }

    private FlightSchedule schedule(LocalDate date, String number) {
        return new FlightSchedule(date, date, Set.of(date.getDayOfWeek()), LocalTime.of(10, 0), LocalTime.of(12, 0),
                new FlightNumber(number), new Airport("BRU"), new Airport("ATH"), new AircraftRegistration("SXBAA"));
    }

    private FlightFacts createFlight() {
        return flights.announceFlights(schedule(DATE, "HJ" + NUMBER.incrementAndGet())).getFirst();
    }

    private long countMessages(FlightId id, String kind) {
        return jdbc.queryForObject("select count(*) from pm_message where flight_key = ? and kind = ?", Long.class, id.key(), kind);
    }

    private void awaitMessages(FlightId id, String kind, long count) {
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertThat(countMessages(id, kind)).isEqualTo(count));
    }

    private void publish(Object event) {
        new TransactionTemplate(transactions).executeWithoutResult(status -> events.publishEvent(event));
    }

    @Test
    void ssmExpandsInclusiveWeekdaysAndDoesNotDuplicateFlightsOrFlms() {
        String number = "HJ" + NUMBER.incrementAndGet();
        FlightSchedule schedule = new FlightSchedule(DATE.minusDays(1), DATE.plusDays(2), Set.of(DATE.getDayOfWeek(), DATE.plusDays(1).getDayOfWeek()),
                LocalTime.of(23, 30), LocalTime.of(1, 0), new FlightNumber(number), new Airport("BRU"), new Airport("ATH"), new AircraftRegistration("SXBAA"));
        ScheduleFeedCommand command = new ScheduleFeedCommand(UUID.randomUUID(), schedule);
        schedules.routeScheduleFeed(command);
        schedules.routeScheduleFeed(command);
        schedules.routeScheduleFeed(new ScheduleFeedCommand(UUID.randomUUID(), schedule));
        List<FlightFacts> created = flights.listFlights(null).stream().filter(f -> f.id().flightNumber().value().equals(number)).toList();
        assertThat(created).hasSize(2);
        assertThat(created).extracting(f -> f.id().scheduledDepartureDate()).containsExactly(DATE, DATE.plusDays(1));
        assertThat(created.getFirst().scheduledArrival()).isEqualTo(DATE.plusDays(1).atTime(1, 0));
        for (FlightFacts flight : created) awaitMessages(flight.id(), "FLM", 1);
    }

    @Test
    void rolledBackFlightCreationDoesNotCreatePartnerMessages() {
        String number = "HJ" + NUMBER.incrementAndGet();
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            flights.announceFlights(schedule(DATE, number));
            status.setRollbackOnly();
        });
        FlightId id = new FlightId(new FlightNumber(number), DATE, new Airport("BRU"));
        assertThatThrownBy(() -> flights.getFlightFacts(id)).isInstanceOf(FlightNotFoundException.class);
        assertThat(countMessages(id, "FLM")).isZero();
    }

    @Test
    @WithMockUser(roles = "flight-planner")
    void delayChangesPublishRequiredMovementsButDescriptionCorrectionDoesNot() {
        FlightFacts flight = createFlight();
        awaitMessages(flight.id(), "FLM", 1);
        FlightFacts delayed = flights.registerDelay(flight.id(), DelayPhase.DEPARTURE, "93", 30, "Aircraft rotation");
        UUID delayId = delayed.delays().getFirst().id();
        awaitMessages(flight.id(), "MVT_ED", 1);
        awaitMessages(flight.id(), "MVT_EA", 1);
        flights.correctDelay(flight.id(), delayId, 30, "Corrected explanation");
        flights.removeDelay(flight.id(), delayId);
        awaitMessages(flight.id(), "MVT_ED", 2);
        awaitMessages(flight.id(), "MVT_EA", 2);
        assertThat(jdbc.queryForList("select body from pm_message where flight_key = ? and kind = 'MVT_ED' order by sequence", String.class, flight.id().key()))
                .containsExactly("MVT\n" + flight.id().flightNumber().value() + String.format("/%02d.SXBAA.BRU\nED1030", DATE.getDayOfMonth()),
                        "MVT\n" + flight.id().flightNumber().value() + String.format("/%02d.SXBAA.BRU\nED1000", DATE.getDayOfMonth()));
    }

    @Test
    @WithMockUser(roles = "flight-planner")
    void departureAndArrivalDelayOwnershipSurvivesPersistenceAndCorrections() {
        FlightFacts flight = createFlight();
        FlightFacts arrivingLate = flights.registerDelay(flight.id(), DelayPhase.ARRIVAL, "93", 15, "Arrival handling");
        UUID arrivalDelay = arrivingLate.delays().getFirst().id();
        FlightFacts departingLate = flights.registerDelay(flight.id(), DelayPhase.DEPARTURE, "93", 30, "Aircraft rotation");
        UUID departureDelay = departingLate.delays().stream().filter(delay -> delay.phase() == DelayPhase.DEPARTURE)
                .findFirst().orElseThrow().id();
        flights.correctDelay(flight.id(), arrivalDelay, 20, "Updated arrival handling");
        FlightFacts reloaded = flights.getFlightFacts(flight.id());
        assertThat(reloaded.estimatedDeparture()).isEqualTo(DATE.atTime(10, 30));
        assertThat(reloaded.estimatedArrival()).isEqualTo(DATE.atTime(12, 50));
        flights.removeDelay(flight.id(), departureDelay);
        FlightFacts withoutDepartureDelay = flights.getFlightFacts(flight.id());
        assertThat(withoutDepartureDelay.estimatedDeparture()).isEqualTo(DATE.atTime(10, 0));
        assertThat(withoutDepartureDelay.estimatedArrival()).isEqualTo(DATE.atTime(12, 20));
        assertThat(withoutDepartureDelay.delays()).extracting(DelayDetails::phase).containsExactly(DelayPhase.ARRIVAL);
        awaitMessages(flight.id(), "MVT_ED", 2);
        awaitMessages(flight.id(), "MVT_EA", 4);
    }

    @Test
    @WithMockUser(roles = "flight-planner")
    void cpmIsDurableAndIdempotentBeforeDepartureIsAllowed() {
        FlightFacts flight = createFlight();
        assertThatThrownBy(() -> flights.recordDeparture(flight.id(), DATE.atTime(10, 0))).hasMessageContaining("CPM");
        LoadFinalisedEvent event = new LoadFinalisedEvent(UUID.randomUUID(), NOW, flight.id(),
                List.of(new PlacedUld("AKE16689R7", "13L", new BigDecimal("1000"), "KG", new Airport("ATH"))));
        publish(event);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertThat(flights.getFlightFacts(flight.id()).finalLoadReceived()).isTrue());
        publish(event);
        awaitMessages(flight.id(), "CPM", 1);
        FlightFacts departed = flights.recordDeparture(flight.id(), DATE.atTime(10, 0));
        assertThat(departed.status()).isEqualTo(FlightStatus.DEPARTED);
        awaitMessages(flight.id(), "MVT_AD", 1);
        assertThat(jdbc.queryForObject("select body from pm_message where flight_key = ? and kind = 'CPM'", String.class, flight.id().key()))
                .contains("AKE16689R7", "13L", "messageId", "formatVersion");
    }

    @Test
    void partnerOutageKeepsPublicationForRetryWithTheSameMessageIdentityAndBody() {
        doThrow(new IllegalStateException("Broker unavailable")).doNothing()
                .when(rabbit).send(anyString(), anyString(), any(Message.class));
        FlightFacts flight = createFlight();
        awaitMessages(flight.id(), "FLM", 1);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertThat(jdbc.queryForObject(
                "select count(*) from event_publication where status = 'FAILED'", Long.class)).isPositive());
        assertThat(flights.getFlightFacts(flight.id())).isNotNull();
        failedPublications.resubmit(ResubmissionOptions.defaults());
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertThat(jdbc.queryForObject(
                "select count(*) from event_publication", Long.class)).isZero());
        org.mockito.ArgumentCaptor<Message> messages = org.mockito.ArgumentCaptor.forClass(Message.class);
        verify(rabbit, times(2)).send(eq("airfreight.partner-messages"), eq("flm"), messages.capture());
        assertThat(messages.getAllValues().get(0).getBody()).isEqualTo(messages.getAllValues().get(1).getBody());
        assertThat(messages.getAllValues().get(0).getMessageProperties().getMessageId())
                .isEqualTo(messages.getAllValues().get(1).getMessageProperties().getMessageId());
        assertThat(countMessages(flight.id(), "FLM")).isEqualTo(1);
    }

    @Test
    void committedEventIsRetriedFromPersistentRegistryAfterListenerFailure() {
        doThrow(new IllegalStateException("Transient outbox failure")).doCallRealMethod().when(messageStore).saveIfAbsent(any());
        FlightFacts flight = createFlight();
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertThat(jdbc.queryForObject(
                "select count(*) from event_publication where status = 'FAILED'", Long.class)).isPositive());
        failedPublications.resubmit(ResubmissionOptions.defaults());
        awaitMessages(flight.id(), "FLM", 1);
    }

    @Test
    @WithMockUser(roles = "flight-planner")
    void versionRejectsAnUpdateBasedOnAnOlderFlightSnapshot() {
        FlightFacts flight = createFlight();
        awaitMessages(flight.id(), "FLM", 1);
        TransactionTemplate concurrentChange = new TransactionTemplate(transactions);
        concurrentChange.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        assertThatThrownBy(() -> new TransactionTemplate(transactions).executeWithoutResult(status -> {
            flights.getFlightFacts(flight.id());
            // Another committed transaction changes the row after this transaction loaded it.
            concurrentChange.executeWithoutResult(other -> jdbc.update(
                    "update fo_flight set version = version + 1 where id = ?", flight.id().key()));
            flights.registerDelay(flight.id(), DelayPhase.DEPARTURE, "93", 10, "Aircraft rotation");
        })).isInstanceOf(OptimisticLockingFailureException.class);

        assertThat(flights.getFlightFacts(flight.id()).delays()).isEmpty();
        assertThat(countMessages(flight.id(), "MVT_ED")).isZero();
    }

    @Test
    @WithMockUser(roles = {"admin", "flight-planner"})
    void retentionRemovesFlightsAndTheirStoredMessagesRegardlessOfState() {
        updatePolicy.updatePolicy(new OperationsPolicy(OperationsPolicy.Severity.HARD, 720, 20, new BigDecimal("10"), 30, 5));
        FlightFacts expired = flights.announceFlights(schedule(DATE.minusDays(6), "HJ" + NUMBER.incrementAndGet())).getFirst();
        FlightFacts kept = flights.announceFlights(schedule(DATE.minusDays(5), "HJ" + NUMBER.incrementAndGet())).getFirst();
        awaitMessages(expired.id(), "FLM", 1);
        awaitMessages(kept.id(), "FLM", 1);
        flights.registerDelay(expired.id(), DelayPhase.DEPARTURE, "93", 10, "Aircraft rotation");
        awaitMessages(expired.id(), "MVT_ED", 1);
        updatePolicy.updatePolicy(OperationsPolicy.defaults());
        flights.removeExpiredFlights();
        assertThatThrownBy(() -> flights.getFlightFacts(expired.id())).isInstanceOf(FlightNotFoundException.class);
        assertThat(flights.getFlightFacts(kept.id())).isNotNull();
        assertThat(jdbc.queryForObject("select count(*) from fo_delay where flight_id = ?", Long.class, expired.id().key())).isZero();
        awaitMessages(expired.id(), "FLM", 0);
    }

    @Test
    void missingLoadImplementationRefusesBookingWithoutRecordingReceipt() {
        UUID messageId = UUID.randomUUID();
        CargoBooking booking = new CargoBooking(messageId, createFlight().id(), "AKE16689R7", new BigDecimal("1000"), "KG", new Airport("ATH"));
        assertThatThrownBy(() -> cargo.routeCargoFeed(booking)).isInstanceOf(LoadPlanningUnavailableException.class);
        assertThat(jdbc.queryForObject("select count(*) from oc_feed_receipt where message_id = ?", Long.class, messageId)).isZero();
    }

    @Test
    void httpEnforcesRolesAndReturnsExplicitRefusals() throws Exception {
        FlightFacts flight = createFlight();
        String url = "/api/flights/" + flight.id().flightNumber().value() + "/" + DATE + "/BRU";
        mvc.perform(get("/api/flights")).andExpect(status().isUnauthorized());
        mvc.perform(post(url + "/departure").with(user("test-user").roles("load-planner")).with(csrf())
                .contentType("application/json").content(json.writeValueAsString(Map.of("actualTime", DATE.atTime(10, 0))))).andExpect(status().isForbidden());
        mvc.perform(post(url + "/departure").with(user("test-user").roles("flight-planner")).with(csrf())
                .contentType("application/json").content(json.writeValueAsString(Map.of("actualTime", DATE.atTime(10, 0)))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("CPM")));
        mvc.perform(post(url + "/delays").with(user("test-user").roles("flight-planner")).with(csrf())
                .contentType("application/json").content("{\"phase\":\"DEPARTURE\",\"code\":\"93\",\"minutes\":0,\"description\":\"Invalid\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/flights/HJ9999/" + DATE + "/BRU").with(user("test-user").roles("admin")).with(csrf()))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/policy").with(user("test-user").roles("flight-planner")).with(csrf())
                .contentType("application/json").content(json.writeValueAsString(OperationsPolicy.defaults()))).andExpect(status().isForbidden());
    }

    @Test
    void policyUpdatesPersistAndOnlyAdminCanChangeThem() throws Exception {
        OperationsPolicy updated = new OperationsPolicy(OperationsPolicy.Severity.SOFT, 720, 20, new BigDecimal("12"), 10, 10);
        mvc.perform(put("/api/policy").with(user("test-user").roles("admin")).with(csrf())
                .contentType("application/json").content(json.writeValueAsString(updated)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.actualTimeToleranceMinutes").value(10));
        assertThat(getPolicy.currentPolicy()).isEqualTo(updated);
        assertThat(jdbc.queryForObject("select actual_time_tolerance_minutes from oc_policy where id = 1", Integer.class)).isEqualTo(10);
        mvc.perform(put("/api/policy").with(user("test-user").roles("admin")).with(csrf())
                .contentType("application/json").content("{\"flightRetentionDays\":-1}"))
                .andExpect(status().isBadRequest());
        assertThat(getPolicy.currentPolicy()).isEqualTo(updated);
    }

    @Test
    void mockPlannerStillNeedsCsrfForSessionAuthenticatedChanges() throws Exception {
        FlightFacts flight = createFlight();
        String url = "/api/flights/" + flight.id().key() + "/departure";
        mvc.perform(post(url).with(user("planner").roles("flight-planner"))
                .contentType("application/json")
                .content(json.writeValueAsString(Map.of("actualTime", DATE.atTime(10, 0)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "admin")
    void lateFinalLoadAfterRetentionDoesNotRecreateMessagesOrBlockEventRecovery() {
        updatePolicy.updatePolicy(new OperationsPolicy(OperationsPolicy.Severity.HARD, 720, 20, new BigDecimal("10"), 30, 5));
        FlightFacts old = flights.announceFlights(schedule(DATE.minusDays(6), "HJ" + NUMBER.incrementAndGet())).getFirst();
        awaitMessages(old.id(), "FLM", 1);
        updatePolicy.updatePolicy(OperationsPolicy.defaults());
        flights.removeExpiredFlights();
        awaitMessages(old.id(), "FLM", 0);
        publish(new LoadFinalisedEvent(UUID.randomUUID(), NOW, old.id(), List.of()));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertThat(jdbc.queryForObject(
                "select count(*) from event_publication", Long.class)).isZero());
        assertThat(countMessages(old.id(), "CPM")).isZero();
    }

    @Test
    @WithMockUser(roles = "flight-planner")
    void newPolicyToleranceAppliesToAnExistingFlight() throws Exception {
        FlightFacts flight = createFlight();
        flights.processCPM(flight.id());
        LocalDateTime departure = DATE.atTime(10, 10);
        assertThatThrownBy(() -> flights.recordDeparture(flight.id(), departure)).hasMessageContaining("delay");
        OperationsPolicy updated = new OperationsPolicy(OperationsPolicy.Severity.HARD, 720, 20, new BigDecimal("10"), 7, 10);
        mvc.perform(put("/api/policy").with(user("test-user").roles("admin")).with(csrf())
                .contentType("application/json").content(json.writeValueAsString(updated))).andExpect(status().isOk());
        // MockMvc replaces the thread's authentication, so invoke the planner action through HTTP too.
        mvc.perform(post("/api/flights/" + flight.id().flightNumber().value() + "/" + DATE + "/BRU/departure")
                .with(user("test-user").roles("flight-planner")).with(csrf())
                .contentType("application/json").content(json.writeValueAsString(Map.of("actualTime", DATE.atTime(10, 10)))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DEPARTED"));
    }
}
