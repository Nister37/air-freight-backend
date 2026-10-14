package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.common.FlightId;

public interface RemoveLoadForFlightUseCase {
    void removeLoadForFlight(FlightId id);
}
