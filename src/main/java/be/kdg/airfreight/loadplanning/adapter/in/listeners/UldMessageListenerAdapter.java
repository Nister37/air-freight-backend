package be.kdg.airfreight.loadplanning.adapter.in.listeners;

import be.kdg.airfreight.loadplanning.adapter.config.LoadPlanningRabbitTopology;
import be.kdg.airfreight.loadplanning.port.in.usecase.ReceiveCargoFeedUseCase;
import org.slf4j.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
class UldMessageListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(UldMessageListenerAdapter.class);
    private final ReceiveCargoFeedUseCase useCase;
    UldMessageListenerAdapter(ReceiveCargoFeedUseCase useCase) { this.useCase = useCase; }

    @RabbitListener(queues = LoadPlanningRabbitTopology.QUEUE, autoStartup = "${airfreight.feeds.cargo.enabled:false}")
    void handleUldMessage(UldMessage payload, Message message) {
        LOGGER.info("Processing ULD message ID {}", message.getMessageProperties().getMessageId());
        useCase.receiveCargoFeed(payload.command());
    }
}
