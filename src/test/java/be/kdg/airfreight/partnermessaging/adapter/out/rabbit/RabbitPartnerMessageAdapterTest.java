package be.kdg.airfreight.partnermessaging.adapter.out.rabbit;

import be.kdg.airfreight.partnermessaging.events.PartnerMessageCreatedEvent;
import be.kdg.airfreight.common.*;
import be.kdg.airfreight.partnermessaging.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RabbitPartnerMessageAdapterTest {
    private PartnerMessage message() {
        return new PartnerMessage(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.parse("2026-10-11T10:00:00"), 1,
                new FlightId(new FlightNumber("HJ221"), LocalDate.of(2026, 10, 11), new Airport("BRU")),
                MessageKind.MVT_AD, "MVT\nHJ221/11.SXBAA.BRU\nAD1000");
    }

    @Test
    void sendsPersistentTelegramWithRequiredMetadata() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        PartnerMessage message = message();
        new RabbitPartnerMessageAdapter(rabbit).publish(new PartnerMessageCreatedEvent(message));
        org.mockito.ArgumentCaptor<Message> sent = org.mockito.ArgumentCaptor.forClass(Message.class);
        verify(rabbit).send(eq("airfreight.partner-messages"), eq("mvt.ad"), sent.capture());
        Message wire = sent.getValue();
        assertThat(wire.getMessageProperties().getMessageId()).isEqualTo(message.id().toString());
        assertThat(wire.getMessageProperties().getContentType()).isEqualTo("text/plain");
        assertThat(wire.getMessageProperties().getHeaders().get("formatVersion")).isEqualTo(1);
        assertThat(wire.getMessageProperties().getHeaders().get("producedAt")).isEqualTo(message.producedAt().toString());
        assertThat(wire.getMessageProperties().getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
        assertThat(new String(wire.getBody(), StandardCharsets.UTF_8)).isEqualTo(message.body());
    }

    @Test
    void brokerFailureEscapesTheListenerSoItsRegistryPublicationCanBeRetried() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        doThrow(new AmqpException("Broker unavailable")).when(rabbit).send(anyString(), anyString(), any(Message.class));
        assertThatThrownBy(() -> new RabbitPartnerMessageAdapter(rabbit).publish(new PartnerMessageCreatedEvent(message())))
                .isInstanceOf(AmqpException.class).hasMessageContaining("Broker unavailable");
    }
}
