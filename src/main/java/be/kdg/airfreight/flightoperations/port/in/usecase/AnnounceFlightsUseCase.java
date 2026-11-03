package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.flightoperations.domain.vo.FlightSchedule;
import java.util.*;

public interface AnnounceFlightsUseCase {
    List<Flight> announceFlights(FlightSchedule schedule);
}
