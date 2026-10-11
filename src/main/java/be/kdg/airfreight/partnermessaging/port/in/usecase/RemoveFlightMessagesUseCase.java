package be.kdg.airfreight.partnermessaging.port.in.usecase;

import be.kdg.airfreight.common.events.FlightRemovedEvent;

public interface RemoveFlightMessagesUseCase {
    void removeFlightMessages(FlightRemovedEvent event);
}
