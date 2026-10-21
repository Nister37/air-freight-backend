package be.kdg.airfreight.flightoperations.adapter.in.listeners;

import be.kdg.airfreight.flightoperations.adapter.config.FlightOperationsRabbitTopology;
import be.kdg.airfreight.flightoperations.port.in.usecase.ReceiveScheduleFeedUseCase;
import org.slf4j.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
class SsmMessageListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SsmMessageListenerAdapter.class);
    private final ReceiveScheduleFeedUseCase useCase;
    SsmMessageListenerAdapter(ReceiveScheduleFeedUseCase useCase) { this.useCase = useCase; }

    @RabbitListener(queues = FlightOperationsRabbitTopology.QUEUE)
    void handleSsmMessage(SsmMessage payload, Message message) {
        LOGGER.info("Processing SSM message ID {}", message.getMessageProperties().getMessageId());
        useCase.receiveScheduleFeed(payload.command());
    }
}
