package be.kdg.airfreight.flightoperations.port.out;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightSelection;
import java.time.LocalDate;
import java.util.*;

public interface FlightLoadPort {
    Optional<Flight> load(FlightId id);
    List<Flight> findFlights(FlightSelection selection);
    List<Flight> loadExpired(LocalDate lastDepartureDate);
}
