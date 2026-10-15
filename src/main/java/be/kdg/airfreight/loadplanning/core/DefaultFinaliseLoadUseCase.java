package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.common.OperationsPolicy;
import be.kdg.airfreight.common.PolicyQuery;
import be.kdg.airfreight.loadplanning.LoadNotFoundException;
import be.kdg.airfreight.loadplanning.domain.FloorPlan;
import be.kdg.airfreight.loadplanning.domain.Load;
import be.kdg.airfreight.loadplanning.domain.LoadRules;
import be.kdg.airfreight.loadplanning.domain.Severity;
import be.kdg.airfreight.loadplanning.port.in.command.FinaliseLoadCommand;
import be.kdg.airfreight.loadplanning.port.in.usecase.FinaliseLoadUseCase;
import be.kdg.airfreight.loadplanning.port.out.AircraftLoadPort;
import be.kdg.airfreight.loadplanning.port.out.LoadEventPort;
import be.kdg.airfreight.loadplanning.port.out.LoadLoadPort;
import be.kdg.airfreight.loadplanning.port.out.LoadUpdatePort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Transactional
class DefaultFinaliseLoadUseCase implements FinaliseLoadUseCase {

    private final LoadLoadPort loadPort;
    private final LoadUpdatePort updatePort;
    private final LoadEventPort eventPort;
    private final AircraftLoadPort aircraftPort;
    private final PolicyQuery policyQuery;

    DefaultFinaliseLoadUseCase(LoadLoadPort loadPort, LoadUpdatePort updatePort, LoadEventPort eventPort,
                               AircraftLoadPort aircraftPort, PolicyQuery policyQuery) {
        this.loadPort = loadPort;
        this.updatePort = updatePort;
        this.eventPort = eventPort;
        this.aircraftPort = aircraftPort;
        this.policyQuery = policyQuery;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_load-planner')")
    public LoadFacts finaliseLoad(FinaliseLoadCommand command) {
        Load load = required(command.flightId());
        load.finalise(command.confirmWarnings(), floorPlanFor(load), currentRules(),
                LocalDateTime.now(ZoneOffset.UTC));
        return save(load);
    }

    private LoadRules currentRules() {
        OperationsPolicy policy = policyQuery.currentPolicy();
        return new LoadRules(
                Severity.valueOf(policy.offloadingConstraint().name()),
                policy.maximumLateralWeightDifferencePercent());
    }

    private Load required(FlightId id) {
        return loadPort.load(id)
                .orElseThrow(() -> new LoadNotFoundException("No load for flight " + id.key()));
    }

    private FloorPlan floorPlanFor(Load load) {
        return aircraftPort.floorPlanFor(load.aircraftRegistration())
                .orElseThrow(() -> new IllegalStateException(
                        "No floor plan known for aircraft " + load.aircraftRegistration().value()));
    }

    private LoadFacts save(Load load) {
        updatePort.update(load);
        load.getDomainEvents().forEach(eventPort::publish);
        load.clearDomainEvents();
        return load.facts();
    }

}
