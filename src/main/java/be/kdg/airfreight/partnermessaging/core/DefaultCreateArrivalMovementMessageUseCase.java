package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.common.messages.MessageKind;
import be.kdg.airfreight.partnermessaging.port.in.command.CreateActualMovementMessageCommand;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateArrivalMovementMessageUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultCreateArrivalMovementMessageUseCase implements CreateArrivalMovementMessageUseCase {
    private final PartnerMessageCreation messages;

    DefaultCreateArrivalMovementMessageUseCase(PartnerMessageCreation messages) {
        this.messages = messages;
    }

    @Override
    public void createArrivalMovementMessage(CreateActualMovementMessageCommand command) {
        messages.createTelegram(command.sourceEventId(), command.producedAt(), command.flightId(),
                command.aircraftRegistration(), command.airport(),
                MessageKind.MVT_AA, command.movementTime());
    }
}
