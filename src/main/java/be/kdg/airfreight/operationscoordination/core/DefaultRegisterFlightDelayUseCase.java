package be.kdg.airfreight.operationscoordination.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.RegisterDelayCommand;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RegisterFlightDelayUseCase;
import be.kdg.airfreight.operationscoordination.port.out.FlightRequestPort;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
class DefaultRegisterFlightDelayUseCase implements RegisterFlightDelayUseCase {
    private final FlightRequestPort flights;
    DefaultRegisterFlightDelayUseCase(FlightRequestPort flights) { this.flights = flights; }

    @Override
    public FlightFacts registerDelay(RegisterDelayCommand command) { return flights.registerDelay(command.flightId(), command.phase(), command.code(), command.minutes(), command.description()); }
}
