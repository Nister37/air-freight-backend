package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.common.*;

public interface FlightExistsQuery {
    boolean flightExists(FlightId id);
}
