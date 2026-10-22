package be.kdg.airfreight.partnermessaging.port.in.usecase;

import be.kdg.airfreight.common.FlightId;

public interface RemoveFlightMessagesUseCase {
    void removeFlightMessages(FlightId flightId);
}
