package be.kdg.airfreight.partnermessaging.adapter.out.rabbit;

import be.kdg.airfreight.partnermessaging.events.PartnerMessageCreatedEvent;
import be.kdg.airfreight.partnermessaging.adapter.out.messaging.MvtTelegramConverter;
import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.events.DomainEvent;
import be.kdg.airfreight.partnermessaging.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import tools.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RabbitPartnerMessageAdapterTest {
    private RabbitTemplate jsonTemplate() {
        RabbitTemplate rabbit = spy(new RabbitTemplate(mock(ConnectionFactory.class)));
        rabbit.setMessageConverter(new JacksonJsonMessageConverter(new JsonMapper(), DomainEvent.class.getPackageName()));
        doNothing().when(rabbit).send(anyString(), anyString(), any(Message.class), isNull());
        return rabbit;
    }

    private RabbitTemplate telegramTemplate() {
        RabbitTemplate rabbit = spy(new RabbitTemplate(mock(ConnectionFactory.class)));
        rabbit.setMessageConverter(new MvtTelegramConverter());
        doNothing().when(rabbit).send(anyString(), anyString(), any(Message.class), isNull());
        return rabbit;
    }

    private PartnerMessage message() {
        return new PartnerMessage(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.parse("2026-10-11T10:00:00"), 1,
                new FlightId(new FlightNumber("HJ221"), LocalDate.of(2026, 10, 11), new Airport("BRU")),
                MessageKind.MVT_AD, "MVT\nHJ221/11.SXBAA.BRU\nAD1000");
    }

    @Test
    void sendsPersistentTelegramWithRequiredMetadata() {
        RabbitTemplate rabbit = telegramTemplate();
        PartnerMessage message = message();
        new RabbitPartnerMessageAdapter(jsonTemplate(), rabbit).publish(new PartnerMessageCreatedEvent(message));
        org.mockito.ArgumentCaptor<Message> sent = org.mockito.ArgumentCaptor.forClass(Message.class);
        verify(rabbit).send(eq("airfreight.partner-messages"), eq("mvt.ad"), sent.capture(), isNull());
        Message wire = sent.getValue();
        assertThat(wire.getMessageProperties().getMessageId()).isEqualTo(message.id().toString());
        assertThat(wire.getMessageProperties().getContentType()).isEqualTo("text/plain");
        assertThat(wire.getMessageProperties().getContentEncoding()).isEqualTo("UTF-8");
        assertThat(wire.getMessageProperties().getHeaders().get("formatVersion")).isEqualTo(1);
        assertThat(wire.getMessageProperties().getHeaders().get("producedAt")).isEqualTo(message.producedAt().toString());
        assertThat(wire.getMessageProperties().getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
        assertThat(new String(wire.getBody(), StandardCharsets.UTF_8)).isEqualTo(message.body());
    }

    @Test
    void sendsSavedJsonWithoutQuotingOrReserializingIt() {
        RabbitTemplate rabbit = jsonTemplate();
        PartnerMessage base = message();
        String body = "{\n  \"messageId\": \"" + base.id() + "\",\n  \"data\": {\"flights\": []}\n}";
        PartnerMessage message = new PartnerMessage(base.id(), base.sourceEventId(), base.producedAt(),
                base.formatVersion(), base.flightId(), MessageKind.FLM, body);
        new RabbitPartnerMessageAdapter(rabbit, telegramTemplate()).publish(new PartnerMessageCreatedEvent(message));

        org.mockito.ArgumentCaptor<Message> sent = org.mockito.ArgumentCaptor.forClass(Message.class);
        verify(rabbit).send(eq("airfreight.partner-messages"), eq("flm"), sent.capture(), isNull());
        Message wire = sent.getValue();
        assertThat(wire.getBody()).isEqualTo(body.getBytes(StandardCharsets.UTF_8));
        assertThat(wire.getMessageProperties().getContentType()).isEqualTo("application/json");
        assertThat(wire.getMessageProperties().getContentEncoding()).isEqualTo("UTF-8");
        assertThat(wire.getMessageProperties().getMessageId()).isEqualTo(message.id().toString());
        assertThat(wire.getMessageProperties().getHeaders().get("formatVersion")).isEqualTo(message.formatVersion());
        assertThat(wire.getMessageProperties().getHeaders().get("producedAt")).isEqualTo(message.producedAt().toString());
        assertThat(wire.getMessageProperties().getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
    }

    @Test
    void brokerFailureEscapesTheListenerSoItsRegistryPublicationCanBeRetried() {
        RabbitTemplate rabbit = telegramTemplate();
        doThrow(new AmqpException("Broker unavailable")).when(rabbit)
                .send(anyString(), anyString(), any(Message.class), isNull());
        assertThatThrownBy(() -> new RabbitPartnerMessageAdapter(jsonTemplate(), rabbit).publish(new PartnerMessageCreatedEvent(message())))
                .isInstanceOf(AmqpException.class).hasMessageContaining("Broker unavailable");
    }
}
