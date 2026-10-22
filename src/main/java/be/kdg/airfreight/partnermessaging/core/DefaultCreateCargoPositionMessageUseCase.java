package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.partnermessaging.port.in.command.CreateCargoPositionMessageCommand;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateCargoPositionMessageUseCase;
import be.kdg.airfreight.partnermessaging.port.out.FinalLoadPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultCreateCargoPositionMessageUseCase implements CreateCargoPositionMessageUseCase {
    private final PartnerMessageCreation messages;
    private final FinalLoadPort finalLoadPort;

    DefaultCreateCargoPositionMessageUseCase(PartnerMessageCreation messages, FinalLoadPort finalLoadPort) {
        this.messages = messages;
        this.finalLoadPort = finalLoadPort;
    }

    @Override
    public void createCargoPositionMessage(CreateCargoPositionMessageCommand command) {
        if (messages.createCargoPositions(command.sourceEventId(), command.producedAt(), command.flightId(), command.placedUlds())) {
            finalLoadPort.processCPM(command.flightId());
        }
    }
}
