package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.partnermessaging.port.in.command.CreateFlightListMessageCommand;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateFlightListMessageUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultCreateFlightListMessageUseCase implements CreateFlightListMessageUseCase {
    private final PartnerMessageCreation messages;

    DefaultCreateFlightListMessageUseCase(PartnerMessageCreation messages) {
        this.messages = messages;
    }

    @Override
    public void createFlightListMessage(CreateFlightListMessageCommand command) {
        messages.createFlightList(command.sourceEventId(), command.producedAt(), command.flight());
    }
}
