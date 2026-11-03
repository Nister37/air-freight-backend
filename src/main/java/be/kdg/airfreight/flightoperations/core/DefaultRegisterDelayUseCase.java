package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.flightoperations.domain.entity.Delay;
import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.flightoperations.domain.vo.DelayCode;
import be.kdg.airfreight.flightoperations.domain.vo.DelayId;

import be.kdg.airfreight.flightoperations.port.in.command.RegisterDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.usecase.RegisterDelayUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultRegisterDelayUseCase implements RegisterDelayUseCase {
    private final FlightLookup flightLookup;
    private final List<FlightUpdatePort> updatePorts;

    DefaultRegisterDelayUseCase(FlightLookup flightLookup, List<FlightUpdatePort> updatePorts) {
        this.flightLookup = flightLookup;
        this.updatePorts = List.copyOf(updatePorts);
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_flight-planner')")
    public Flight registerDelay(RegisterDelayCommand command) {
        Flight flight = flightLookup.required(command.flightId());
        Delay delay = new Delay(DelayId.create(), new DelayCode(command.code()), command.description(), command.minutes());
        switch (command.phase()) {
            case DEPARTURE -> flight.registerDepartureDelay(delay);
            case ARRIVAL -> flight.registerArrivalDelay(delay);
        }
        return save(flight);
    }

    private Flight save(Flight flight) {
        updatePorts.forEach(port -> port.update(flight));
        return flight;
    }
}
