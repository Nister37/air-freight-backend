package be.kdg.airfreight.partnermessaging.adapter.out.rabbit;

import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.FlightNumber;
import be.kdg.airfreight.partnermessaging.domain.enums.MessageKind;
import be.kdg.airfreight.partnermessaging.domain.PartnerMessage;
import be.kdg.airfreight.partnermessaging.events.PartnerMessageCreatedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PartnerMessageRabbitPublisherAdapterTest {
    @Test
    void sendsTheSavedFlmAsJsonWithItsMessageIdentity() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        RabbitTemplate telegrams = mock(RabbitTemplate.class);
        UUID id = UUID.randomUUID();
        String body = "{\"messageId\":\"" + id + "\",\"data\":{\"flightNumber\":\"HJ221\"}}";
        PartnerMessage flm = new PartnerMessage(id, UUID.randomUUID(), LocalDateTime.of(2026, 10, 11, 10, 0), 1,
                new FlightId(new FlightNumber("HJ221"), LocalDate.of(2026, 10, 11), new Airport("BRU")), MessageKind.FLM, body);

        new PartnerMessageRabbitPublisherAdapter(rabbit, telegrams).publish(new PartnerMessageCreatedEvent(flm));

        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        verify(rabbit).send(eq("airfreight.partner-messages"), eq("flm"), sent.capture());
        assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), sent.getValue().getBody());
        assertEquals("application/json", sent.getValue().getMessageProperties().getContentType());
        assertEquals(id.toString(), sent.getValue().getMessageProperties().getMessageId());
        assertEquals(MessageDeliveryMode.PERSISTENT, sent.getValue().getMessageProperties().getDeliveryMode());
    }
}
