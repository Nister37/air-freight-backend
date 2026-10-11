package be.kdg.airfreight.partnermessaging.port.in.usecase;

import be.kdg.airfreight.common.events.FlightDepartedEvent;

public interface CreateDepartureMovementMessageUseCase {
    void createDepartureMovementMessage(FlightDepartedEvent event);
}
