package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.partnermessaging.domain.enums.MessageKind;
import be.kdg.airfreight.partnermessaging.port.in.command.CreateActualMovementMessageCommand;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateDepartureMovementMessageUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultCreateDepartureMovementMessageUseCase implements CreateDepartureMovementMessageUseCase {
    private final PartnerMessageCreation messages;

    DefaultCreateDepartureMovementMessageUseCase(PartnerMessageCreation messages) {
        this.messages = messages;
    }

    @Override
    public void createDepartureMovementMessage(CreateActualMovementMessageCommand command) {
        messages.createTelegram(command.sourceEventId(), command.producedAt(), command.flightId(),
                command.aircraftRegistration(), command.airport(),
                MessageKind.MVT_AD, command.movementTime());
    }
}
