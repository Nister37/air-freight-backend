package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.flightoperations.domain.vo.DelayId;

import be.kdg.airfreight.flightoperations.port.in.command.CorrectDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.usecase.CorrectDelayUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultCorrectDelayUseCase implements CorrectDelayUseCase {
    private final FlightLookup flightLookup;
    private final List<FlightUpdatePort> updatePorts;

    DefaultCorrectDelayUseCase(FlightLookup flightLookup, List<FlightUpdatePort> updatePorts) {
        this.flightLookup = flightLookup;
        this.updatePorts = List.copyOf(updatePorts);
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_flight-planner')")
    public Flight correctDelay(CorrectDelayCommand command) {
        Flight flight = flightLookup.required(command.flightId());
        flight.correctDelay(new DelayId(command.delayId()), command.minutes(), command.description());
        return save(flight);
    }

    private Flight save(Flight flight) {
        updatePorts.forEach(port -> port.update(flight));
        return flight;
    }
}
