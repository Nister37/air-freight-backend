package be.kdg.airfreight.operationscoordination.adapter.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;

@Configuration
public class FeedRabbitTopology {
    public static final String EXCHANGE = "airfreight.feeds";
    public static final String SSM_QUEUE = "airfreight.ssm";
    public static final String ULD_QUEUE = "airfreight.uld";
    public static final String SSM_ROUTING_KEY = "ssm";
    public static final String ULD_ROUTING_KEY = "uld";

    private static final String DEAD_LETTER_EXCHANGE = EXCHANGE + ".dlx";
    private static final String SSM_DEAD_LETTER_QUEUE = SSM_QUEUE + ".dlq";
    private static final String ULD_DEAD_LETTER_QUEUE = ULD_QUEUE + ".dlq";

    @Bean TopicExchange feedExchange() { return new TopicExchange(EXCHANGE); }
    @Bean TopicExchange feedDeadLetterExchange() { return new TopicExchange(DEAD_LETTER_EXCHANGE); }
    @Bean Queue ssmQueue() { return QueueBuilder.durable(SSM_QUEUE).deadLetterExchange(DEAD_LETTER_EXCHANGE).deadLetterRoutingKey(SSM_ROUTING_KEY).build(); }
    @Bean Queue uldQueue() { return QueueBuilder.durable(ULD_QUEUE).deadLetterExchange(DEAD_LETTER_EXCHANGE).deadLetterRoutingKey(ULD_ROUTING_KEY).build(); }
    @Bean Queue ssmDeadLetterQueue() { return QueueBuilder.durable(SSM_DEAD_LETTER_QUEUE).build(); }
    @Bean Queue uldDeadLetterQueue() { return QueueBuilder.durable(ULD_DEAD_LETTER_QUEUE).build(); }
    @Bean Binding ssmBinding() { return BindingBuilder.bind(ssmQueue()).to(feedExchange()).with(SSM_ROUTING_KEY); }
    @Bean Binding uldBinding() { return BindingBuilder.bind(uldQueue()).to(feedExchange()).with(ULD_ROUTING_KEY); }
    @Bean Binding ssmDeadLetterBinding() { return BindingBuilder.bind(ssmDeadLetterQueue()).to(feedDeadLetterExchange()).with(SSM_ROUTING_KEY); }
    @Bean Binding uldDeadLetterBinding() { return BindingBuilder.bind(uldDeadLetterQueue()).to(feedDeadLetterExchange()).with(ULD_ROUTING_KEY); }
}
