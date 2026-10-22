package be.kdg.airfreight.loadplanning.adapter.in.listeners;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.messages.UldMessage;
import be.kdg.airfreight.flightoperations.FlightOperationsApi;
import be.kdg.airfreight.loadplanning.LoadPlanningApi;
import be.kdg.airfreight.loadplanning.adapter.config.LoadPlanningRabbitTopology;
import be.kdg.airfreight.loadplanning.port.in.usecase.GetLoadQuery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.listener.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CargoFeedIntegrationTest {
    @Autowired private RabbitListenerEndpointRegistry listeners;
    @Autowired private FlightOperationsApi flights;
    @Autowired private GetLoadQuery loads;
    @Autowired private LoadPlanningApi loadPlanning;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper mapper;
    @Autowired private JacksonJsonMessageConverter jsonMessageConverter;
    @MockitoBean(name = "rabbitTemplate") private RabbitTemplate rabbit;
    @MockitoBean(name = "mvtRabbitTemplate") private RabbitTemplate telegrams;

    @Test
    void cargoJsonIsBookedOnceAndRedeliveryIsIgnoredEvenAfterTheFlightIsRemoved() throws Exception {
        FlightId flight = announceFlight("HJ608");
        ChannelAwareMessageListener listener = cargoListener();
        UUID messageId = UUID.randomUUID();
        Message message = cargoMessage(messageId, flight);
        message.getMessageProperties().setHeader("__TypeId__", UldMessage.class.getName());
        assertInstanceOf(UldMessage.class, jsonMessageConverter.fromMessage(message));

        listener.onMessage(message, null);
        Message redelivery = cargoMessage(messageId, flight);
        redelivery.getMessageProperties().setHeader("__TypeId__",
                "be.kdg.airfreight.simulation.adapter.out.rabbit.SimulationRabbitPublisherAdapter$UldMessage");
        listener.onMessage(redelivery, null);

        assertEquals(1, loads.getLoad(flight).ulds().size());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM lp_feed_receipt WHERE message_id = ?",
                Integer.class, messageId));
        jdbc.update("DELETE FROM fo_flight WHERE id = ?", flight.key());
        assertDoesNotThrow(() -> listener.onMessage(message, null));
    }

    @Test
    void receiptFailureRollsBackTheBookingAndAllowsRedelivery() throws Exception {
        FlightId flight = announceFlight("HJ609");
        ChannelAwareMessageListener listener = cargoListener();
        UUID messageId = UUID.randomUUID();
        Message message = cargoMessage(messageId, flight);
        jdbc.execute("ALTER TABLE lp_feed_receipt ADD CONSTRAINT reject_cargo_receipt "
                + "CHECK (message_id <> CAST('" + messageId + "' AS UUID))");
        try {
            assertThrows(ListenerExecutionFailedException.class, () -> listener.onMessage(message, null));
        } finally {
            jdbc.execute("ALTER TABLE lp_feed_receipt DROP CONSTRAINT reject_cargo_receipt");
        }

        assertTrue(loads.getLoad(flight).ulds().isEmpty());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM lp_feed_receipt WHERE message_id = ?",
                Integer.class, messageId));
        listener.onMessage(message, null);
        assertEquals(1, loads.getLoad(flight).ulds().size());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM lp_feed_receipt WHERE message_id = ?",
                Integer.class, messageId));
    }

    @Test
    void finalisingLoadPersistsAndSendsTheCpmAndProcessesTheFinalLoad() throws Exception {
        FlightId flight = announceFlight("HJ611");
        assertFalse(flights.getFlightFacts(flight).finalLoadReceived());
        ChannelAwareMessageListener listener = cargoListener();
        listener.onMessage(cargoMessage(UUID.randomUUID(), flight, "AKE11111AA"), null);
        listener.onMessage(cargoMessage(UUID.randomUUID(), flight, "AKE22222AA"), null);

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("load-planner", null, "ROLE_load-planner"));
        try {
            loadPlanning.placeULD(flight, "AKE11111AA", "CL");
            loadPlanning.placeULD(flight, "AKE22222AA", "CR");
            assertTrue(loadPlanning.finaliseLoad(flight, false).finalised());
        } finally {
            SecurityContextHolder.clearContext();
        }

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertTrue(flights.getFlightFacts(flight).finalLoadReceived());
            verify(rabbit).send(eq("airfreight.partner-messages"), eq("cpm"), any(Message.class));
        });
        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        verify(rabbit).send(eq("airfreight.partner-messages"), eq("cpm"), sent.capture());
        Message wire = sent.getValue();
        var envelope = mapper.readTree(wire.getBody());
        var data = envelope.get("data");
        assertEquals(4, envelope.size());
        assertEquals(4, data.size());
        assertEquals("HJ611", data.get("flightNumber").asString());
        assertEquals(flight.scheduledDepartureDate().toString(), data.get("scheduledDepartureDate").asString());
        assertEquals("BRU", data.get("departureAirport").asString());
        assertEquals(2, data.get("placedUlds").size());
        Map<String, String> positions = Map.of("AKE11111AA", "CL", "AKE22222AA", "CR");
        Set<String> codes = new HashSet<>();
        for (var placed : data.get("placedUlds")) {
            String code = placed.get("uldCode").asString();
            codes.add(code);
            assertEquals(5, placed.size());
            assertEquals(positions.get(code), placed.get("position").asString());
            assertEquals(900, placed.get("weight").asInt());
            assertEquals("KG", placed.get("unit").asString());
            assertEquals("ATH", placed.get("destination").get("value").asString());
        }
        assertEquals(positions.keySet(), codes);
        assertEquals(wire.getMessageProperties().getMessageId(), envelope.get("messageId").asString());
        assertEquals(1, envelope.get("formatVersion").asInt());
        assertEquals(1, wire.getMessageProperties().getHeaders().get("formatVersion"));
        assertEquals(LocalDateTime.parse(envelope.get("producedAt").asString()),
                LocalDateTime.parse(wire.getMessageProperties().getHeaders().get("producedAt").toString()));
        assertEquals(new String(wire.getBody(), StandardCharsets.UTF_8), jdbc.queryForObject(
                "SELECT body FROM pm_message WHERE flight_key = ? AND kind = 'CPM'", String.class, flight.key()));
        assertEquals(1, jdbc.queryForObject("SELECT format_version FROM pm_message WHERE flight_key = ? AND kind = 'CPM'",
                Integer.class, flight.key()));
    }

    private FlightId announceFlight(String number) {
        LocalDateTime std = LocalDateTime.now(ZoneOffset.UTC).withNano(0).plusHours(2);
        FlightId flight = flights.announceFlights(new FlightSchedule(std.toLocalDate(), std.toLocalDate(),
                Set.of(std.getDayOfWeek()), std.toLocalTime(), std.plusHours(2).toLocalTime(),
                new FlightNumber(number), new Airport("BRU"), new Airport("ATH"),
                new AircraftRegistration("SXBAA"))).getFirst().id();
        await().atMost(Duration.ofSeconds(10)).ignoreExceptions()
                .untilAsserted(() -> assertNotNull(loads.getLoad(flight)));
        return flight;
    }

    private ChannelAwareMessageListener cargoListener() {
        return listeners.getListenerContainers().stream()
                .map(AbstractMessageListenerContainer.class::cast)
                .filter(container -> Arrays.asList(container.getQueueNames()).contains(LoadPlanningRabbitTopology.QUEUE))
                .map(container -> (ChannelAwareMessageListener) container.getMessageListener())
                .findFirst().orElseThrow();
    }

    private Message cargoMessage(UUID messageId, FlightId flight) {
        return cargoMessage(messageId, flight, "AKE11111AA");
    }

    private Message cargoMessage(UUID messageId, FlightId flight, String uldCode) {
        String json = mapper.writeValueAsString(Map.of("messageId", messageId, "flightNumber", flight.flightNumber().value(),
                "departureDay", flight.scheduledDepartureDate().getDayOfMonth(), "departureAirport", "BRU", "uldCode", uldCode,
                "weight", 900, "unit", "KG", "destination", "ATH"));
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(messageId.toString());
        properties.setContentType("application/json");
        return new Message(json.getBytes(StandardCharsets.UTF_8), properties);
    }
}
