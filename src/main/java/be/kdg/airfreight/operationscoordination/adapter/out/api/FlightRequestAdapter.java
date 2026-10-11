package be.kdg.airfreight.operationscoordination.adapter.out.api;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.FlightOperationsApi;
import be.kdg.airfreight.operationscoordination.port.out.FlightRequestPort;
import org.springframework.stereotype.Component;
import java.time.*;
import java.util.*;

@Component
class FlightRequestAdapter implements FlightRequestPort {
    private final FlightOperationsApi flights;
    FlightRequestAdapter(FlightOperationsApi flights) { this.flights = flights; }
    public List<FlightFacts> announceFlights(FlightSchedule schedule) { return flights.announceFlights(schedule); }
    public FlightFacts registerDelay(FlightId id, DelayPhase phase, String code, int minutes, String description) { return flights.registerDelay(id, phase, code, minutes, description); }
    public FlightFacts correctDelay(FlightId id, UUID delayId, int minutes, String description) { return flights.correctDelay(id, delayId, minutes, description); }
    public FlightFacts removeDelay(FlightId id, UUID delayId) { return flights.removeDelay(id, delayId); }
    public FlightFacts recordDeparture(FlightId id, LocalDateTime at) { return flights.recordDeparture(id, at); }
    public FlightFacts recordArrival(FlightId id, LocalDateTime at) { return flights.recordArrival(id, at); }
    public FlightFacts getFlightFacts(FlightId id) { return flights.getFlightFacts(id); }
    public List<FlightFacts> listFlights(LocalDate date) { return flights.listFlights(date); }
}
