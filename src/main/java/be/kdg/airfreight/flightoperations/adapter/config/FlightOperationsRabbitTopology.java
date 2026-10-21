package be.kdg.airfreight.flightoperations.adapter.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlightOperationsRabbitTopology {
    public static final String EXCHANGE = "airfreight.feeds";
    public static final String QUEUE = "airfreight.ssm";
    public static final String ROUTING_KEY = "ssm";
    private static final String DEAD_LETTER_EXCHANGE = EXCHANGE + ".dlx";
    private static final String DEAD_LETTER_QUEUE = QUEUE + ".dlq";

    @Bean
    TopicExchange flightOperationsFeedExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    TopicExchange flightOperationsFeedDeadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    Queue flightOperationsFeedQueue() {
        return QueueBuilder.durable(QUEUE).deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(ROUTING_KEY).build();
    }

    @Bean
    Queue flightOperationsFeedDeadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding flightOperationsFeedBinding() {
        return BindingBuilder.bind(flightOperationsFeedQueue()).to(flightOperationsFeedExchange()).with(ROUTING_KEY);
    }

    @Bean
    Binding flightOperationsFeedDeadLetterBinding() {
        return BindingBuilder.bind(flightOperationsFeedDeadLetterQueue()).to(flightOperationsFeedDeadLetterExchange())
                .with(ROUTING_KEY);
    }
}
