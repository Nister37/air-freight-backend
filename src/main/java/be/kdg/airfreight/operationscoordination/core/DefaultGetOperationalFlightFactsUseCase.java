package be.kdg.airfreight.operationscoordination.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.usecase.GetOperationalFlightFactsUseCase;
import be.kdg.airfreight.operationscoordination.port.out.FlightRequestPort;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
class DefaultGetOperationalFlightFactsUseCase implements GetOperationalFlightFactsUseCase {
    private final FlightRequestPort flights;
    DefaultGetOperationalFlightFactsUseCase(FlightRequestPort flights) { this.flights = flights; }

    @Override
    public FlightFacts getFlightFacts(FlightId id) { return flights.getFlightFacts(id); }
}
