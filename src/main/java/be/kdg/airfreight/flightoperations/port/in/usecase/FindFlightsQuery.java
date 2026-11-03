package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import java.util.List;

public interface FindFlightsQuery {
    List<Flight> findFlights(FlightSelection selection);
}
