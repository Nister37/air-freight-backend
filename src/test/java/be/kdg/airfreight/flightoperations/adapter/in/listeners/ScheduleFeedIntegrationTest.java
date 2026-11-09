package be.kdg.airfreight.flightoperations.adapter.in.listeners;

import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.common.messages.SsmMessage;
import be.kdg.airfreight.flightoperations.port.in.usecase.FindFlightsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightSelection;
import be.kdg.airfreight.flightoperations.adapter.config.FlightOperationsRabbitTopology;
import be.kdg.airfreight.partnermessaging.port.in.command.CreateFlightListMessageCommand;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateFlightListMessageUseCase;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Autowired private FindFlightsQuery findFlights;
    @Autowired private CreateFlightListMessageUseCase flightListMessages;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper mapper;
    @Autowired private JacksonJsonMessageConverter jsonMessageConverter;
    @MockitoBean(name = "rabbitTemplate") private RabbitTemplate rabbit;
    @MockitoBean(name = "mvtRabbitTemplate") private RabbitTemplate telegrams;

    @Test
    void scheduleJsonCreatesFlightsAndSendsOneFlmPerFlightEvenAfterRedelivery() throws Exception {
        LocalDate from = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        LocalDate to = from.plusDays(1);
        UUID messageId = UUID.randomUUID();
        Message input = schedule(messageId, from, to);
        input.getMessageProperties().setHeader("__TypeId__", SsmMessage.class.getName());
        assertInstanceOf(SsmMessage.class, jsonMessageConverter.fromMessage(input));

        deliver(input);
        Message redelivery = schedule(messageId, from, to);
        redelivery.getMessageProperties().setHeader("__TypeId__",
                "be.kdg.airfreight.simulation.adapter.out.rabbit.SimulationRabbitPublisherAdapter$SsmMessage");
        deliver(redelivery);
        deliver(schedule(UUID.randomUUID(), from, to));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            verify(rabbit, times(2)).send(eq("partner-exchange"), eq("flm"), any(Message.class));
            assertEquals(0L, jdbc.queryForObject("select count(*) from events.event_publication", Long.class));
        });
        List<Flight> created = findFlights.findFlights(new FlightSelection(null, null, null, null, null));
        assertEquals(List.of(from, to), created.stream().map(flight -> flight.getId().scheduledDepartureDate()).toList());
        assertEquals(from.plusDays(1).atTime(1, 0), created.getFirst().getScheduledArrival());
        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        verify(rabbit, times(2)).send(eq("partner-exchange"), eq("flm"), sent.capture());
        Set<LocalDate> announcedDates = new HashSet<>();
        for (Message wire : sent.getAllValues()) {
            var envelope = mapper.readTree(wire.getBody());
            var data = envelope.get("data");
            assertEquals(4, envelope.size());
            assertEquals(6, data.size());
            LocalDate date = LocalDate.parse(data.get("scheduledDepartureDate").asString());
            announcedDates.add(date);
            assertEquals("HJ221", data.get("flightNumber").asString());
            assertEquals("BRU", data.get("departureAirport").asString());
            assertEquals("ATH", data.get("arrivalAirport").asString());
            assertEquals(date.atTime(23, 30), java.time.LocalDateTime.parse(data.get("STD").asString()));
            assertEquals(date.plusDays(1).atTime(1, 0), java.time.LocalDateTime.parse(data.get("STA").asString()));
            assertEquals(wire.getMessageProperties().getMessageId(), envelope.get("messageId").asString());
            assertEquals(1, envelope.get("formatVersion").asInt());
            assertEquals(1, wire.getMessageProperties().getHeaders().get("formatVersion"));
            assertEquals(LocalDateTime.parse(envelope.get("producedAt").asString()),
                    LocalDateTime.parse(wire.getMessageProperties().getHeaders().get("producedAt").toString()));
        }
        assertEquals(Set.of(from, to), announcedDates);

        // The same input port can be called directly without creating another message for the source event.
        Message firstWire = sent.getAllValues().getFirst();
        var firstEnvelope = mapper.readTree(firstWire.getBody());
        UUID firstMessageId = UUID.fromString(firstEnvelope.get("messageId").asString());
        UUID sourceEventId = jdbc.queryForObject("SELECT source_event_id FROM partnermessaging.pm_message WHERE message_id = ?",
                UUID.class, firstMessageId);
        LocalDate firstDate = LocalDate.parse(firstEnvelope.get("data").get("scheduledDepartureDate").asString());
        Flight firstFlight = created.stream()
                .filter(flight -> flight.getId().scheduledDepartureDate().equals(firstDate)).findFirst().orElseThrow();
        var command = new CreateFlightListMessageCommand(sourceEventId,
                LocalDateTime.parse(firstEnvelope.get("producedAt").asString()), firstFlight.getId(),
                firstFlight.getArrivalAirport(), firstFlight.getScheduledDeparture(), firstFlight.getScheduledArrival());
        flightListMessages.createFlightListMessage(command);
        flightListMessages.createFlightListMessage(command);
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM partnermessaging.pm_message WHERE kind = 'FLM'", Integer.class));
        assertEquals(new String(firstWire.getBody(), java.nio.charset.StandardCharsets.UTF_8),
                jdbc.queryForObject("SELECT body FROM partnermessaging.pm_message WHERE message_id = ?", String.class, firstMessageId));
        verify(rabbit, times(2)).send(eq("partner-exchange"), eq("flm"), any(Message.class));
    }

    @Test
    void receiptFailureRollsBackEveryFlightInTheSchedule() throws Exception {
        LocalDate from = LocalDate.now(ZoneOffset.UTC).plusDays(2);
        UUID messageId = UUID.randomUUID();
        Message input = schedule(messageId, from, from.plusDays(1), "HJ610");
        jdbc.execute("ALTER TABLE flightoperations.fo_feed_receipt ADD CONSTRAINT reject_schedule_receipt "
                + "CHECK (message_id <> CAST('" + messageId + "' AS UUID))");
        try {
            assertThrows(ListenerExecutionFailedException.class, () -> deliver(input));
        } finally {
            jdbc.execute("ALTER TABLE flightoperations.fo_feed_receipt DROP CONSTRAINT reject_schedule_receipt");
        }

        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM flightoperations.fo_flight WHERE flight_number = 'HJ610'", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM flightoperations.fo_feed_receipt WHERE message_id = ?",
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
