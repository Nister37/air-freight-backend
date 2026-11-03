package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.flightoperations.port.in.command.ScheduleFeedCommand;

public interface ReceiveScheduleFeedUseCase {
    void receiveScheduleFeed(ScheduleFeedCommand command);
}
