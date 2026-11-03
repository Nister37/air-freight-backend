package be.kdg.airfreight.flightoperations.adapter.in.listeners;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightNumber;
import be.kdg.airfreight.flightoperations.domain.vo.FlightSchedule;
import be.kdg.airfreight.common.messages.SsmMessage;
import be.kdg.airfreight.flightoperations.adapter.config.FlightOperationsRabbitTopology;
import be.kdg.airfreight.flightoperations.port.in.command.ScheduleFeedCommand;
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
        FlightSchedule schedule = new FlightSchedule(payload.from(), payload.to(), payload.weekdays(),
                payload.departureTime(), payload.arrivalTime(), new FlightNumber(payload.flightNumber()),
                new Airport(payload.departureAirport()), new Airport(payload.arrivalAirport()),
                new AircraftRegistration(payload.aircraftRegistration()));
        useCase.receiveScheduleFeed(new ScheduleFeedCommand(payload.messageId(), schedule));
    }
}
