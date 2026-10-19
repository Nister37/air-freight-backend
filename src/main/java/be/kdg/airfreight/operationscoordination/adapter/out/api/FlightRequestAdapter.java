package be.kdg.airfreight.operationscoordination.adapter.out.api;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.FlightOperationsApi;
import be.kdg.airfreight.operationscoordination.port.out.FlightRequestPort;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
class FlightRequestAdapter implements FlightRequestPort {
    private final FlightOperationsApi flights;
    FlightRequestAdapter(FlightOperationsApi flights) { this.flights = flights; }
    public List<FlightFacts> announceFlights(FlightSchedule schedule) { return flights.announceFlights(schedule); }
}
