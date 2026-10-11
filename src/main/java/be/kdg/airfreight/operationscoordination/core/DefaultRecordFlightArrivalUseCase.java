package be.kdg.airfreight.operationscoordination.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.RecordActualTimeCommand;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RecordFlightArrivalUseCase;
import be.kdg.airfreight.operationscoordination.port.out.FlightRequestPort;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
class DefaultRecordFlightArrivalUseCase implements RecordFlightArrivalUseCase {
    private final FlightRequestPort flights;
    DefaultRecordFlightArrivalUseCase(FlightRequestPort flights) { this.flights = flights; }

    @Override
    public FlightFacts recordArrival(RecordActualTimeCommand command) { return flights.recordArrival(command.flightId(), command.actualTime()); }
}
