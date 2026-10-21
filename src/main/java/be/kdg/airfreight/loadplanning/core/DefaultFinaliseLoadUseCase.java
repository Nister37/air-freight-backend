package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.loadplanning.domain.LoadPolicy;
import be.kdg.airfreight.loadplanning.domain.FloorPlan;
import be.kdg.airfreight.loadplanning.domain.Load;
import be.kdg.airfreight.loadplanning.domain.LoadRules;
import be.kdg.airfreight.loadplanning.domain.LoadChecks;
import be.kdg.airfreight.loadplanning.port.in.command.FinaliseLoadCommand;
import be.kdg.airfreight.loadplanning.port.in.usecase.FinaliseLoadUseCase;
import be.kdg.airfreight.loadplanning.port.out.AircraftLoadPort;
import be.kdg.airfreight.loadplanning.port.out.LoadUpdatePort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional
class DefaultFinaliseLoadUseCase implements FinaliseLoadUseCase {

    private final LoadLookup loadLookup;
    private final List<LoadUpdatePort> updatePorts;
    private final AircraftLoadPort aircraftPort;
    private final LoadPolicyReader policyReader;

    DefaultFinaliseLoadUseCase(LoadLookup loadLookup, List<LoadUpdatePort> updatePorts,
                               AircraftLoadPort aircraftPort, LoadPolicyReader policyReader) {
        this.loadLookup = loadLookup;
        this.updatePorts = List.copyOf(updatePorts);
        this.aircraftPort = aircraftPort;
        this.policyReader = policyReader;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_load-planner')")
    public LoadFacts finaliseLoad(FinaliseLoadCommand command) {
        Load load = loadLookup.required(command.flightId());
        load.finalise(command.confirmWarnings(), floorPlanFor(load), currentChecks(),
                LocalDateTime.now(ZoneOffset.UTC));
        return save(load);
    }

    private LoadChecks currentChecks() {
        LoadPolicy policy = policyReader.current();
        return LoadChecks.standard(new LoadRules(
                policy.offloadingConstraint(),
                policy.maximumLateralWeightDifferencePercent()));
    }

    private FloorPlan floorPlanFor(Load load) {
        return aircraftPort.floorPlanFor(load.aircraftRegistration())
                .orElseThrow(() -> new IllegalStateException(
                        "No floor plan known for aircraft " + load.aircraftRegistration().value()));
    }

    private LoadFacts save(Load load) {
        updatePorts.forEach(port -> port.update(load));
        return load.facts();
    }

}
