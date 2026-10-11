package be.kdg.airfreight.partnermessaging.adapter.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;

@Configuration
public class PartnerRabbitTopology {
    public static final String EXCHANGE = "airfreight.partner-messages";
    public static final String QUEUE = "airfreight.partners";

    private static final String ALL_ROUTING_KEYS = "#";

    @Bean TopicExchange partnerExchange() { return new TopicExchange(EXCHANGE); }
    @Bean Queue partnerQueue() { return QueueBuilder.durable(QUEUE).build(); }
    @Bean Binding partnerBinding() { return BindingBuilder.bind(partnerQueue()).to(partnerExchange()).with(ALL_ROUTING_KEYS); }
}
