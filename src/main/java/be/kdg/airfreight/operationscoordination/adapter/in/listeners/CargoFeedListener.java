package be.kdg.airfreight.operationscoordination.adapter.in.listeners;

import be.kdg.airfreight.operationscoordination.adapter.config.FeedRabbitTopology;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RouteCargoFeedUseCase;
import org.slf4j.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
class CargoFeedListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CargoFeedListener.class);
    private final RouteCargoFeedUseCase useCase;
    private final JsonMapper mapper;
    CargoFeedListener(RouteCargoFeedUseCase useCase, JsonMapper mapper) { this.useCase = useCase; this.mapper = mapper; }

    @RabbitListener(queues = FeedRabbitTopology.ULD_QUEUE, autoStartup = "${airfreight.feeds.cargo.enabled:false}")
    void receive(Message message) {
        LOGGER.info("Processing ULD message ID {}", message.getMessageProperties().getMessageId());
        useCase.routeCargoFeed(mapper.readValue(message.getBody(), UldMessage.class).booking());
    }
}
