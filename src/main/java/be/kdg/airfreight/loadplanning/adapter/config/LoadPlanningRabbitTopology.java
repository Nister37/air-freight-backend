package be.kdg.airfreight.loadplanning.adapter.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadPlanningRabbitTopology {
    public static final String EXCHANGE = "airfreight.feeds";
    public static final String QUEUE = "airfreight.uld";
    public static final String ROUTING_KEY = "uld";
    private static final String DEAD_LETTER_EXCHANGE = EXCHANGE + ".dlx";
    private static final String DEAD_LETTER_QUEUE = QUEUE + ".dlq";

    @Bean
    TopicExchange loadPlanningFeedExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    TopicExchange loadPlanningFeedDeadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    Queue loadPlanningFeedQueue() {
        return QueueBuilder.durable(QUEUE).deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(ROUTING_KEY).build();
    }

    @Bean
    Queue loadPlanningFeedDeadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding loadPlanningFeedBinding() {
        return BindingBuilder.bind(loadPlanningFeedQueue()).to(loadPlanningFeedExchange()).with(ROUTING_KEY);
    }

    @Bean
    Binding loadPlanningFeedDeadLetterBinding() {
        return BindingBuilder.bind(loadPlanningFeedDeadLetterQueue()).to(loadPlanningFeedDeadLetterExchange())
                .with(ROUTING_KEY);
    }
}
