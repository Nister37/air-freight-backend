package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.common.PolicyQuery;
import be.kdg.airfreight.loadplanning.domain.LoadRules;
import be.kdg.airfreight.loadplanning.domain.Severity;
import be.kdg.airfreight.loadplanning.domain.FloorPlan;
import be.kdg.airfreight.loadplanning.domain.Load;
import be.kdg.airfreight.loadplanning.domain.Position;
import be.kdg.airfreight.loadplanning.domain.ULDCode;
import be.kdg.airfreight.loadplanning.port.in.command.PlaceULDCommand;
import be.kdg.airfreight.loadplanning.port.in.usecase.PlaceULDUseCase;
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
class DefaultPlaceULDUseCase implements PlaceULDUseCase {

    private final LoadLookup loadLookup;
    private final List<LoadUpdatePort> updatePorts;
    private final AircraftLoadPort aircraftPort;
    private final PolicyQuery policyQuery;

    DefaultPlaceULDUseCase(LoadLookup loadLookup, List<LoadUpdatePort> updatePorts,
                           AircraftLoadPort aircraftPort, PolicyQuery policyQuery) {
        this.loadLookup = loadLookup;
        this.updatePorts = List.copyOf(updatePorts);
        this.aircraftPort = aircraftPort;
        this.policyQuery = policyQuery;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_load-planner')")
    public LoadFacts placeULD(PlaceULDCommand command) {
        Load load = loadLookup.required(command.flightId());
        FloorPlan floorPlan = floorPlanFor(load);

        Position position  =floorPlan.find(command.position())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Position " + command.position() + " does not exist on this aircraft"));

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        load.place(new ULDCode(command.uldCode()), position, floorPlan, now);
        var policy = policyQuery.currentPolicy();
        load.check(floorPlan, new LoadRules(Severity.valueOf(policy.offloadingConstraint().name()),
                policy.maximumLateralWeightDifferencePercent()), now);
        return save(load);
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
