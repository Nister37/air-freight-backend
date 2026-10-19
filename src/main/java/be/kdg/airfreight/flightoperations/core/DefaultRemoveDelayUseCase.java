package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.domain.*;
import be.kdg.airfreight.flightoperations.port.in.command.RemoveDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.usecase.RemoveDelayUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultRemoveDelayUseCase implements RemoveDelayUseCase {
    private final FlightLookup flightLookup;
    private final List<FlightUpdatePort> updatePorts;

    DefaultRemoveDelayUseCase(FlightLookup flightLookup, List<FlightUpdatePort> updatePorts) {
        this.flightLookup = flightLookup;
        this.updatePorts = List.copyOf(updatePorts);
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_flight-planner')")
    public FlightFacts removeDelay(RemoveDelayCommand command) {
        Flight flight = flightLookup.required(command.flightId());
        flight.removeDelay(new DelayId(command.delayId()));
        return save(flight);
    }

    private FlightFacts save(Flight flight) {
        updatePorts.forEach(port -> port.update(flight));
        return flight.facts();
    }
}
