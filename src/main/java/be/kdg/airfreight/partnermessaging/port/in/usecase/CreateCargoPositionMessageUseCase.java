package be.kdg.airfreight.partnermessaging.port.in.usecase;

import be.kdg.airfreight.partnermessaging.port.in.command.CreateCargoPositionMessageCommand;

public interface CreateCargoPositionMessageUseCase {
    void createCargoPositionMessage(CreateCargoPositionMessageCommand command);
}
