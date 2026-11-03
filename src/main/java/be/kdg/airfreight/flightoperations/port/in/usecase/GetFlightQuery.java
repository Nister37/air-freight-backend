package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.common.*;

public interface GetFlightQuery {
    Flight getFlight(FlightId id);
}
