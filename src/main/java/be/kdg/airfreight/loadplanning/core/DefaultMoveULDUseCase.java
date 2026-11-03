package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.loadplanning.domain.aggregate.Load;
import be.kdg.airfreight.loadplanning.domain.vo.FloorPlan;
import be.kdg.airfreight.loadplanning.domain.vo.LoadRules;
import be.kdg.airfreight.loadplanning.domain.vo.Position;
import be.kdg.airfreight.loadplanning.domain.vo.ULDCode;

import be.kdg.airfreight.loadplanning.domain.LoadFacts;
import be.kdg.airfreight.loadplanning.domain.*;
import be.kdg.airfreight.loadplanning.port.in.command.MoveULDCommand;
import be.kdg.airfreight.loadplanning.port.in.usecase.MoveULDUseCase;
import be.kdg.airfreight.loadplanning.port.out.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional
class DefaultMoveULDUseCase implements MoveULDUseCase {
    private final LoadLookup loadLookup;
    private final List<LoadUpdatePort> updatePorts;
    private final AircraftLoadPort aircraftPort;
    private final LoadPolicyReader policyReader;

    DefaultMoveULDUseCase(LoadLookup loadLookup, List<LoadUpdatePort> updatePorts,
                         AircraftLoadPort aircraftPort, LoadPolicyReader policyReader) {
        this.loadLookup = loadLookup;
        this.updatePorts = List.copyOf(updatePorts);
        this.aircraftPort = aircraftPort;
        this.policyReader = policyReader;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_load-planner')")
    public LoadFacts moveULD(MoveULDCommand command) {
        Load load = loadLookup.required(command.flightId());
        FloorPlan floorPlan = aircraftPort.floorPlanFor(load.aircraftRegistration())
                .orElseThrow(() -> new IllegalStateException("No floor plan known for aircraft " + load.aircraftRegistration().value()));
        Position position = floorPlan.find(command.position())
                .orElseThrow(() -> new IllegalArgumentException("Position does not exist on this aircraft"));
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        load.move(new ULDCode(command.uldCode()), position, floorPlan, now);
        var policy = policyReader.current();
        load.check(floorPlan, LoadChecks.standard(new LoadRules(policy.offloadingConstraint(),
                policy.maximumLateralWeightDifferencePercent())), now);
        updatePorts.forEach(port -> port.update(load));
        return load.facts();
    }
}
