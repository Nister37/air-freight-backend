package be.kdg.airfreight.operationscoordination.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.RecordActualTimeCommand;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RecordFlightDepartureUseCase;
import be.kdg.airfreight.operationscoordination.port.out.FlightRequestPort;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
class DefaultRecordFlightDepartureUseCase implements RecordFlightDepartureUseCase {
    private final FlightRequestPort flights;
    DefaultRecordFlightDepartureUseCase(FlightRequestPort flights) { this.flights = flights; }

    @Override
    public FlightFacts recordDeparture(RecordActualTimeCommand command) { return flights.recordDeparture(command.flightId(), command.actualTime()); }
}
