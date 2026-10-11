package be.kdg.airfreight.partnermessaging.port.in.usecase;

import be.kdg.airfreight.common.events.FlightTimesChangedEvent;

public interface CreateEstimatedMovementMessagesUseCase {
    void createEstimatedMovementMessages(FlightTimesChangedEvent event);
}
