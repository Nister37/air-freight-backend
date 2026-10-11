package be.kdg.airfreight.partnermessaging.port.in.usecase;

import be.kdg.airfreight.common.events.FlightCreatedEvent;

public interface CreateFlightListMessageUseCase {
    void createFlightListMessage(FlightCreatedEvent event);
}
