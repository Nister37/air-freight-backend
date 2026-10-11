package be.kdg.airfreight.operationscoordination.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.usecase.ListOperationalFlightsUseCase;
import be.kdg.airfreight.operationscoordination.port.out.FlightRequestPort;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
class DefaultListOperationalFlightsUseCase implements ListOperationalFlightsUseCase {
    private final FlightRequestPort flights;
    DefaultListOperationalFlightsUseCase(FlightRequestPort flights) { this.flights = flights; }

    @Override
    public List<FlightFacts> listFlights(LocalDate scheduledDepartureDate) { return flights.listFlights(scheduledDepartureDate); }
}
