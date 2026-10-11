package be.kdg.airfreight.partnermessaging.adapter.out.rabbit;

import be.kdg.airfreight.partnermessaging.adapter.config.PartnerRabbitTopology;
import be.kdg.airfreight.partnermessaging.domain.PartnerMessage;
import be.kdg.airfreight.partnermessaging.events.PartnerMessageCreatedEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.modulith.events.ApplicationModuleListener;
import java.nio.charset.StandardCharsets;

@Component
class RabbitPartnerMessageAdapter {
    private final RabbitTemplate rabbit;
    RabbitPartnerMessageAdapter(RabbitTemplate rabbit) { this.rabbit = rabbit; }
    @ApplicationModuleListener
    void publish(PartnerMessageCreatedEvent event) {
        send(event.message());
    }

    private void send(PartnerMessage message) {
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(message.id().toString());
        properties.setHeader("producedAt", message.producedAt().toString());
        properties.setHeader("formatVersion", message.formatVersion());
        properties.setContentType(message.kind().isTelegram() ? "text/plain" : "application/json");
        properties.setContentEncoding("UTF-8");
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        rabbit.send(PartnerRabbitTopology.EXCHANGE, message.kind().routingKey(),
                new Message(message.body().getBytes(StandardCharsets.UTF_8), properties));
    }
}
