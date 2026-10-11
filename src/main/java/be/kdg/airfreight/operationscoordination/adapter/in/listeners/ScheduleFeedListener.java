package be.kdg.airfreight.operationscoordination.adapter.in.listeners;

import be.kdg.airfreight.operationscoordination.adapter.config.FeedRabbitTopology;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RouteScheduleFeedUseCase;
import org.slf4j.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
class ScheduleFeedListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleFeedListener.class);
    private final RouteScheduleFeedUseCase useCase;
    private final JsonMapper mapper;
    ScheduleFeedListener(RouteScheduleFeedUseCase useCase, JsonMapper mapper) { this.useCase = useCase; this.mapper = mapper; }

    @RabbitListener(queues = FeedRabbitTopology.SSM_QUEUE)
    void receive(Message message) {
        LOGGER.info("Processing SSM message ID {}", message.getMessageProperties().getMessageId());
        useCase.routeScheduleFeed(mapper.readValue(message.getBody(), SsmMessage.class).command());
    }
}
