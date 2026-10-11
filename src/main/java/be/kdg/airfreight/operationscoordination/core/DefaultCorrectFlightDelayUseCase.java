package be.kdg.airfreight.operationscoordination.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.CorrectDelayCommand;
import be.kdg.airfreight.operationscoordination.port.in.usecase.CorrectFlightDelayUseCase;
import be.kdg.airfreight.operationscoordination.port.out.FlightRequestPort;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
class DefaultCorrectFlightDelayUseCase implements CorrectFlightDelayUseCase {
    private final FlightRequestPort flights;
    DefaultCorrectFlightDelayUseCase(FlightRequestPort flights) { this.flights = flights; }

    @Override
    public FlightFacts correctDelay(CorrectDelayCommand command) { return flights.correctDelay(command.flightId(), command.delayId(), command.minutes(), command.description()); }
}
