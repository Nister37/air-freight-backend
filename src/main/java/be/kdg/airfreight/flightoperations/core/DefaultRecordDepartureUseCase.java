package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.domain.*;
import be.kdg.airfreight.flightoperations.port.in.command.RecordActualTimeCommand;
import be.kdg.airfreight.flightoperations.port.in.usecase.RecordDepartureUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.time.*;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultRecordDepartureUseCase implements RecordDepartureUseCase {
    private final FlightLookup flightLookup;
    private final List<FlightUpdatePort> updatePorts;
    private final PolicyQuery policyQuery;

    DefaultRecordDepartureUseCase(FlightLookup flightLookup, List<FlightUpdatePort> updatePorts, PolicyQuery policyQuery) {
        this.flightLookup = flightLookup;
        this.updatePorts = List.copyOf(updatePorts);
        this.policyQuery = policyQuery;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_flight-planner')")
    public FlightFacts recordDeparture(RecordActualTimeCommand command) {
        Flight flight = flightLookup.required(command.flightId());
        flight.recordDeparture(command.actualTime(), LocalDateTime.now(ZoneOffset.UTC), policyQuery.currentPolicy().actualTimeToleranceMinutes());
        return save(flight);
    }

    private FlightFacts save(Flight flight) {
        updatePorts.forEach(port -> port.update(flight));
        return flight.facts();
    }
}
