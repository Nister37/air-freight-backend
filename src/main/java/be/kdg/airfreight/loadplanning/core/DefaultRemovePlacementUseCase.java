package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.loadplanning.domain.Load;
import be.kdg.airfreight.loadplanning.domain.ULDCode;
import be.kdg.airfreight.loadplanning.port.in.command.RemovePlacementCommand;
import be.kdg.airfreight.loadplanning.port.in.usecase.RemovePlacementUseCase;
import be.kdg.airfreight.loadplanning.port.out.LoadUpdatePort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional
class DefaultRemovePlacementUseCase implements RemovePlacementUseCase {

    private final LoadLookup loadLookup;
    private final List<LoadUpdatePort> updatePorts;

    DefaultRemovePlacementUseCase(LoadLookup loadLookup, List<LoadUpdatePort> updatePorts) {
        this.loadLookup = loadLookup;
        this.updatePorts = List.copyOf(updatePorts);
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_load-planner')")
    public LoadFacts removePlacement(RemovePlacementCommand command) {
        Load load = loadLookup.required(command.flightId());
        load.removePlacement(new ULDCode(command.uldCode()), LocalDateTime.now(ZoneOffset.UTC));
        return save(load);
    }

    private LoadFacts save(Load load) {
        updatePorts.forEach(port -> port.update(load));
        return load.facts();
    }

}
