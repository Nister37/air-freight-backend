package be.kdg.airfreight.flightoperations.adapter.in.listeners;

import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.common.FlightNumber;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightStatus;
import be.kdg.airfreight.flightoperations.FlightOperationsApi;
import be.kdg.airfreight.flightoperations.port.in.usecase.FindFlightsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightSelection;
import be.kdg.airfreight.flightoperations.adapter.config.FlightOperationsRabbitTopology;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.listener.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ScheduleFeedIntegrationTest {
    @Autowired private RabbitListenerEndpointRegistry listeners;
    @Autowired private FlightOperationsApi flights;
    @Autowired private FindFlightsQuery findFlights;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper mapper;
    @MockitoBean(name = "rabbitTemplate") private RabbitTemplate rabbit;
    @MockitoBean(name = "mvtRabbitTemplate") private RabbitTemplate telegrams;

    @Test
    void scheduleJsonCreatesFlightsAndSendsOneFlmPerFlightEvenAfterRedelivery() throws Exception {
        LocalDate from = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        LocalDate to = from.plusDays(1);
        UUID messageId = UUID.randomUUID();
        Message input = schedule(messageId, from, to);

        deliver(input);
        deliver(input);
        deliver(schedule(UUID.randomUUID(), from, to));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            verify(rabbit, times(2)).send(eq("airfreight.partner-messages"), eq("flm"), any(Message.class));
            assertEquals(0L, jdbc.queryForObject("select count(*) from event_publication", Long.class));
        });
        List<FlightFacts> created = flights.listFlights(null);
        assertEquals(List.of(from, to), created.stream().map(flight -> flight.id().scheduledDepartureDate()).toList());
        assertEquals(from.plusDays(1).atTime(1, 0), created.getFirst().scheduledArrival());
        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        verify(rabbit, times(2)).send(eq("airfreight.partner-messages"), eq("flm"), sent.capture());
        Set<LocalDate> announcedDates = new HashSet<>();
        for (Message wire : sent.getAllValues()) {
            var envelope = mapper.readTree(wire.getBody());
            var data = envelope.get("data");
            LocalDate date = LocalDate.parse(data.get("scheduledDepartureDate").asString());
            announcedDates.add(date);
            assertEquals("HJ221", data.get("flightNumber").asString());
            assertEquals("BRU", data.get("departureAirport").asString());
            assertEquals("ATH", data.get("arrivalAirport").asString());
            assertEquals(date.atTime(23, 30), java.time.LocalDateTime.parse(data.get("STD").asString()));
            assertEquals(date.plusDays(1).atTime(1, 0), java.time.LocalDateTime.parse(data.get("STA").asString()));
            assertEquals(wire.getMessageProperties().getMessageId(), envelope.get("messageId").asString());
        }
        assertEquals(Set.of(from, to), announcedDates);

        // Search the persisted SSM flights through the database filters, including every flight state.
        assertEquals(List.of(created.getLast()), findFlights.findFlights(new FlightSelection(null, null, null, null, null)));
        assertEquals(List.of(created.getFirst()), findFlights.findFlights(new FlightSelection(from,
                new FlightNumber("HJ221"), new Airport("BRU"), new Airport("ATH"), FlightStatus.SCHEDULED)));
        assertTrue(findFlights.findFlights(new FlightSelection(from.plusDays(2), null, null, null, null)).isEmpty());
        assertTrue(findFlights.findFlights(new FlightSelection(from, new FlightNumber("HJ999"), null, null, null)).isEmpty());
        assertTrue(findFlights.findFlights(new FlightSelection(from, null, new Airport("AMS"), null, null)).isEmpty());
        assertTrue(findFlights.findFlights(new FlightSelection(from, null, null, new Airport("AMS"), null)).isEmpty());

        jdbc.update("update fo_flight set actual_departure = ? where id = ?",
                from.atTime(23, 0), created.getFirst().id().key());
        assertEquals(FlightStatus.DEPARTED, findFlights.findFlights(new FlightSelection(from,
                null, null, null, FlightStatus.DEPARTED)).getFirst().status());
        assertTrue(findFlights.findFlights(new FlightSelection(from, null, null, null, FlightStatus.SCHEDULED)).isEmpty());

        jdbc.update("update fo_flight set actual_arrival = ? where id = ?",
                from.atTime(23, 45), created.getFirst().id().key());
        assertEquals(FlightStatus.LANDED, findFlights.findFlights(new FlightSelection(from,
                null, null, null, FlightStatus.LANDED)).getFirst().status());
        assertTrue(findFlights.findFlights(new FlightSelection(from, null, null, null, FlightStatus.DEPARTED)).isEmpty());
    }

    @Test
    void receiptFailureRollsBackEveryFlightInTheSchedule() throws Exception {
        LocalDate from = LocalDate.now(ZoneOffset.UTC).plusDays(2);
        UUID messageId = UUID.randomUUID();
        Message input = schedule(messageId, from, from.plusDays(1), "HJ610");
        jdbc.execute("ALTER TABLE fo_feed_receipt ADD CONSTRAINT reject_schedule_receipt "
                + "CHECK (message_id <> CAST('" + messageId + "' AS UUID))");
        try {
            assertThrows(ListenerExecutionFailedException.class, () -> deliver(input));
        } finally {
            jdbc.execute("ALTER TABLE fo_feed_receipt DROP CONSTRAINT reject_schedule_receipt");
        }

        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM fo_flight WHERE flight_number = 'HJ610'", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM fo_feed_receipt WHERE message_id = ?",
                Integer.class, messageId));
    }

    private Message schedule(UUID id, LocalDate from, LocalDate to) {
        return schedule(id, from, to, "HJ221");
    }

    private Message schedule(UUID id, LocalDate from, LocalDate to, String flightNumber) {
        String json = mapper.writeValueAsString(Map.of("messageId", id, "from", from, "to", to,
                "weekdays", Set.of(from.getDayOfWeek(), to.getDayOfWeek()), "departureTime", "23:30:00",
                "arrivalTime", "01:00:00", "flightNumber", flightNumber, "departureAirport", "BRU",
                "arrivalAirport", "ATH", "aircraftRegistration", "SXBAA"));
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(id.toString());
        properties.setContentType("application/json");
        return new Message(json.getBytes(java.nio.charset.StandardCharsets.UTF_8), properties);
    }

    private void deliver(Message message) throws Exception {
        // Use Spring's configured JSON conversion and listener dispatch with the physical broker stopped.
        ChannelAwareMessageListener listener = listeners.getListenerContainers().stream()
                .map(AbstractMessageListenerContainer.class::cast)
                .filter(container -> Arrays.asList(container.getQueueNames()).contains(FlightOperationsRabbitTopology.QUEUE))
                .map(container -> (ChannelAwareMessageListener) container.getMessageListener())
                .findFirst().orElseThrow();
        listener.onMessage(message, null);
    }
}
