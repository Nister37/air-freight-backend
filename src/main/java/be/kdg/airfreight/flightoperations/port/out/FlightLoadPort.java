package be.kdg.airfreight.flightoperations.port.out;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.flightoperations.domain.Flight;
import java.time.LocalDate;
import java.util.*;

public interface FlightLoadPort {
    Optional<Flight> load(FlightId id);
    List<Flight> loadAll();
    List<Flight> loadExpired(LocalDate lastDepartureDate);
}
