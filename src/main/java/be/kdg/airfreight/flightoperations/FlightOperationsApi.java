package be.kdg.airfreight.flightoperations;

import be.kdg.airfreight.common.FlightId;

public interface FlightOperationsApi {
    FlightDetails getFlightDetails(FlightId id);
    boolean flightExists(FlightId id);
    void processCPM(FlightId id);
}
