package be.kdg.airfreight.loadplanning.adapter.in.listeners;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.FlightOperationsApi;
import be.kdg.airfreight.loadplanning.adapter.config.LoadPlanningRabbitTopology;
import be.kdg.airfreight.loadplanning.port.in.usecase.GetLoadQuery;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CargoFeedIntegrationTest {
    @Autowired private RabbitListenerEndpointRegistry listeners;
    @Autowired private FlightOperationsApi flights;
    @Autowired private GetLoadQuery loads;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper mapper;
    @MockitoBean(name = "rabbitTemplate") private RabbitTemplate rabbit;
    @MockitoBean(name = "mvtRabbitTemplate") private RabbitTemplate telegrams;

    @Test
    void cargoJsonIsBookedOnceAndRedeliveryIsIgnoredEvenAfterTheFlightIsRemoved() throws Exception {
        FlightId flight = announceFlight("HJ608");
        ChannelAwareMessageListener listener = cargoListener();
        UUID messageId = UUID.randomUUID();
        Message message = cargoMessage(messageId, flight);

        listener.onMessage(message, null);
        listener.onMessage(message, null);

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
        String json = mapper.writeValueAsString(Map.of("messageId", messageId, "flightNumber", flight.flightNumber().value(),
                "departureDay", flight.scheduledDepartureDate().getDayOfMonth(), "departureAirport", "BRU", "uldCode", "AKE11111AA",
                "weight", 900, "unit", "KG", "destination", "ATH"));
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(messageId.toString());
        properties.setContentType("application/json");
        return new Message(json.getBytes(StandardCharsets.UTF_8), properties);
    }
}
