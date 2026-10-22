package be.kdg.airfreight.partnermessaging.port.in.usecase;

import be.kdg.airfreight.partnermessaging.port.in.command.CreateEstimatedMovementMessagesCommand;

public interface CreateEstimatedMovementMessagesUseCase {
    void createEstimatedMovementMessages(CreateEstimatedMovementMessagesCommand command);
}
