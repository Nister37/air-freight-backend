package be.kdg.airfreight.partnermessaging.port.in.usecase;

import be.kdg.airfreight.partnermessaging.port.in.command.CreateFlightListMessageCommand;

public interface CreateFlightListMessageUseCase {
    void createFlightListMessage(CreateFlightListMessageCommand command);
}
