package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.common.FlightFacts;
import java.util.List;

public interface FindFlightsQuery {
    List<FlightFacts> findFlights(FlightSelection selection);
}
