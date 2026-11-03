package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.loadplanning.port.in.command.CargoFeedCommand;

public interface ReceiveCargoFeedUseCase {
    void receiveCargoFeed(CargoFeedCommand command);
}
