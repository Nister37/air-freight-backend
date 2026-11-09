package be.kdg.airfreight.partnermessaging.adapter.out.rabbit;

import be.kdg.airfreight.common.events.PartnerMessageCreatedEvent;
import be.kdg.airfreight.common.messages.MessageKind;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
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
        LocalDateTime producedAt = LocalDateTime.of(2026, 10, 11, 10, 0);
        int formatVersion = 1;
        String body = "{\"messageId\":\"" + id + "\",\"data\":{\"flightNumber\":\"HJ221\"}}";
        PartnerMessageCreatedEvent event = new PartnerMessageCreatedEvent(
                id, producedAt, formatVersion, MessageKind.FLM, body);
        JsonMapper mapper = JsonMapper.builder().build();
        String serialized = mapper.writeValueAsString(event);
        assertFalse(mapper.readTree(serialized).has("message"));
        PartnerMessageCreatedEvent restored = mapper.readValue(serialized, PartnerMessageCreatedEvent.class);
        assertEquals(event, restored);

        new PartnerMessageRabbitPublisherAdapter(rabbit, telegrams).publish(restored);

        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        verify(rabbit).send(eq("partner-exchange"), eq("flm"), sent.capture());
        assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), sent.getValue().getBody());
        assertEquals("application/json", sent.getValue().getMessageProperties().getContentType());
        assertEquals(id.toString(), sent.getValue().getMessageProperties().getMessageId());
        assertEquals(producedAt.toString(), sent.getValue().getMessageProperties().getHeaders().get("producedAt"));
        assertEquals(formatVersion, sent.getValue().getMessageProperties().getHeaders().get("formatVersion"));
        assertEquals(MessageDeliveryMode.PERSISTENT, sent.getValue().getMessageProperties().getDeliveryMode());
    }
}
