package be.kdg.airfreight.partnermessaging.adapter.out.rabbit;

import be.kdg.airfreight.partnermessaging.adapter.config.PartnerRabbitTopology;
import be.kdg.airfreight.common.messages.MessageKind;
import be.kdg.airfreight.common.events.PartnerMessageCreatedEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.modulith.events.ApplicationModuleListener;
import java.nio.charset.StandardCharsets;

@Component
class PartnerMessageRabbitPublisherAdapter {
    private final RabbitTemplate rabbit;
    private final RabbitTemplate mvtRabbit;

    PartnerMessageRabbitPublisherAdapter(RabbitTemplate rabbitTemplate, RabbitTemplate mvtRabbitTemplate) {
        this.rabbit = rabbitTemplate;
        this.mvtRabbit = mvtRabbitTemplate;
    }

    @ApplicationModuleListener
    void publish(PartnerMessageCreatedEvent event) {
        if (event.kind().isTelegram()) {
            mvtRabbit.convertAndSend(PartnerRabbitTopology.EXCHANGE, routingKey(event.kind()), event.body(),
                    wire -> withMetadata(wire, event));
        } else {
            // Saved JSON is already serialized; the JSON converter would quote a String again.
            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            Message wire = new Message(event.body().getBytes(StandardCharsets.UTF_8), properties);
            rabbit.send(PartnerRabbitTopology.EXCHANGE, routingKey(event.kind()), withMetadata(wire, event));
        }
    }

    private String routingKey(MessageKind kind) {
        return switch (kind) {
            case FLM -> PartnerRabbitTopology.FLM;
            case CPM -> PartnerRabbitTopology.CPM;
            case MVT_ED -> PartnerRabbitTopology.MVT_ED;
            case MVT_EA -> PartnerRabbitTopology.MVT_EA;
            case MVT_AD -> PartnerRabbitTopology.MVT_AD;
            case MVT_AA -> PartnerRabbitTopology.MVT_AA;
        };
    }

    private Message withMetadata(Message wire, PartnerMessageCreatedEvent event) {
        MessageProperties properties = wire.getMessageProperties();
        properties.setMessageId(event.eventUUID().toString());
        properties.setHeader("producedAt", event.eventPit().toString());
        properties.setHeader("formatVersion", event.formatVersion());
        properties.setContentEncoding("UTF-8");
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        return wire;
    }
}
