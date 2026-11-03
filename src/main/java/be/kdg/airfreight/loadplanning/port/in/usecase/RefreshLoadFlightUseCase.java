package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.common.FlightId;

public interface RefreshLoadFlightUseCase {
    void refreshLoadFlight(FlightId flightId);
}
