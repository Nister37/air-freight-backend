package be.kdg.airfreight.partnermessaging.port.in.usecase;

import be.kdg.airfreight.common.events.FlightLandedEvent;

public interface CreateArrivalMovementMessageUseCase {
    void createArrivalMovementMessage(FlightLandedEvent event);
}
