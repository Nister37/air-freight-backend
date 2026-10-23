package be.kdg.airfreight.flightoperations;

import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.FlightSchedule;

import java.time.LocalDate;
import java.util.List;

public interface FlightOperationsApi {
    List<FlightFacts> announceFlights(FlightSchedule schedule);
    FlightFacts getFlightFacts(FlightId id);
    boolean flightExists(FlightId id);
    List<FlightFacts> listFlights(LocalDate scheduledDepartureDate);
    void processCPM(FlightId id);
}
