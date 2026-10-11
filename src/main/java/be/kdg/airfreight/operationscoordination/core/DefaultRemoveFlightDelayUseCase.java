package be.kdg.airfreight.operationscoordination.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.RemoveDelayCommand;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RemoveFlightDelayUseCase;
import be.kdg.airfreight.operationscoordination.port.out.FlightRequestPort;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
class DefaultRemoveFlightDelayUseCase implements RemoveFlightDelayUseCase {
    private final FlightRequestPort flights;
    DefaultRemoveFlightDelayUseCase(FlightRequestPort flights) { this.flights = flights; }

    @Override
    public FlightFacts removeDelay(RemoveDelayCommand command) { return flights.removeDelay(command.flightId(), command.delayId()); }
}
