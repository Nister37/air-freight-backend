package be.kdg.airfreight.operationscoordination.adapter.in.listeners;

import be.kdg.airfreight.operationscoordination.adapter.config.FeedRabbitTopology;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RouteScheduleFeedUseCase;
import org.slf4j.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
class ScheduleFeedListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleFeedListener.class);
    private final RouteScheduleFeedUseCase useCase;
    ScheduleFeedListener(RouteScheduleFeedUseCase useCase) { this.useCase = useCase; }

    @RabbitListener(queues = FeedRabbitTopology.SSM_QUEUE)
    void receive(SsmMessage payload, Message message) {
        LOGGER.info("Processing SSM message ID {}", message.getMessageProperties().getMessageId());
        useCase.routeScheduleFeed(payload.command());
    }
}
