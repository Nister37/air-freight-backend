package be.kdg.airfreight.flightoperations;

import be.kdg.airfreight.common.*;
import java.time.*;
import java.util.*;

public interface FlightOperationsApi {
    List<FlightFacts> announceFlights(FlightSchedule schedule);
    FlightFacts registerDelay(FlightId id, DelayPhase phase, String code, int minutes, String description);
    FlightFacts correctDelay(FlightId id, UUID delayId, int minutes, String description);
    FlightFacts removeDelay(FlightId id, UUID delayId);
    FlightFacts recordDeparture(FlightId id, LocalDateTime actualTime);
    FlightFacts recordArrival(FlightId id, LocalDateTime actualTime);
    FlightFacts getFlightFacts(FlightId id);
    boolean flightExists(FlightId id);
    List<FlightFacts> listFlights(LocalDate scheduledDepartureDate);
    void processCPM(FlightId id);
    void removeExpiredFlights();
}
