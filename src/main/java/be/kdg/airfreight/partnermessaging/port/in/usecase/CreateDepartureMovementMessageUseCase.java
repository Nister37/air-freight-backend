package be.kdg.airfreight.partnermessaging.port.in.usecase;

import be.kdg.airfreight.partnermessaging.port.in.command.CreateActualMovementMessageCommand;

public interface CreateDepartureMovementMessageUseCase {
    void createDepartureMovementMessage(CreateActualMovementMessageCommand command);
}
