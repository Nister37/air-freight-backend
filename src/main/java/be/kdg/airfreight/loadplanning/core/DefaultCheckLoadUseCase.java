package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.loadplanning.domain.LoadPolicy;
import be.kdg.airfreight.loadplanning.domain.FloorPlan;
import be.kdg.airfreight.loadplanning.domain.Load;
import be.kdg.airfreight.loadplanning.domain.LoadRules;
import be.kdg.airfreight.loadplanning.domain.LoadChecks;
import be.kdg.airfreight.loadplanning.port.in.usecase.CheckLoadUseCase;
import be.kdg.airfreight.loadplanning.port.out.AircraftLoadPort;
import be.kdg.airfreight.loadplanning.port.out.LoadUpdatePort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional
class DefaultCheckLoadUseCase implements CheckLoadUseCase {

    private final LoadLookup loadLookup;
    private final List<LoadUpdatePort> updatePorts;
    private final AircraftLoadPort aircraftPort;
    private final LoadPolicyReader policyReader;

    DefaultCheckLoadUseCase(LoadLookup loadLookup, List<LoadUpdatePort> updatePorts,
                            AircraftLoadPort aircraftPort, LoadPolicyReader policyReader) {
        this.loadLookup = loadLookup;
        this.updatePorts = List.copyOf(updatePorts);
        this.aircraftPort = aircraftPort;
        this.policyReader = policyReader;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_load-planner')")
    public LoadFacts checkLoad(FlightId id){
        Load load = loadLookup.required(id);
        load.check(floorPlanFor(load), currentChecks(), LocalDateTime.now(ZoneOffset.UTC));
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
