package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.common.messages.MessageKind;
import be.kdg.airfreight.partnermessaging.port.in.command.CreateEstimatedMovementMessagesCommand;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateEstimatedMovementMessagesUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultCreateEstimatedMovementMessagesUseCase implements CreateEstimatedMovementMessagesUseCase {
    private final PartnerMessageCreation messages;

    DefaultCreateEstimatedMovementMessagesUseCase(PartnerMessageCreation messages) {
        this.messages = messages;
    }

    @Override
    public void createEstimatedMovementMessages(CreateEstimatedMovementMessagesCommand command) {
        if (command.departureChanged()) {
            messages.createTelegram(command.sourceEventId(), command.producedAt(), command.flightId(),
                    command.aircraftRegistration(), command.flightId().departureAirport(),
                    MessageKind.MVT_ED, command.estimatedDeparture());
        }
        if (command.arrivalChanged()) {
            messages.createTelegram(command.sourceEventId(), command.producedAt(), command.flightId(),
                    command.aircraftRegistration(), command.arrivalAirport(),
                    MessageKind.MVT_EA, command.estimatedArrival());
        }
    }
}
