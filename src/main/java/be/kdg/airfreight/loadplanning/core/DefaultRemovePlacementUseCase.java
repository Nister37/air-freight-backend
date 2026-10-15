package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.loadplanning.LoadNotFoundException;
import be.kdg.airfreight.loadplanning.domain.Load;
import be.kdg.airfreight.loadplanning.domain.ULDCode;
import be.kdg.airfreight.loadplanning.port.in.command.RemovePlacementCommand;
import be.kdg.airfreight.loadplanning.port.in.usecase.RemovePlacementUseCase;
import be.kdg.airfreight.loadplanning.port.out.LoadEventPort;
import be.kdg.airfreight.loadplanning.port.out.LoadLoadPort;
import be.kdg.airfreight.loadplanning.port.out.LoadUpdatePort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Transactional
class DefaultRemovePlacementUseCase implements RemovePlacementUseCase {

    private final LoadLoadPort loadPort;
    private final LoadUpdatePort updatePort;
    private final LoadEventPort eventPort;

    DefaultRemovePlacementUseCase(LoadLoadPort loadPort, LoadUpdatePort updatePort, LoadEventPort eventPort) {
        this.loadPort = loadPort;
        this.updatePort = updatePort;
        this.eventPort = eventPort;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_load-planner')")
    public LoadFacts removePlacement(RemovePlacementCommand command) {
        Load load = required(command.flightId());
        load.removePlacement(new ULDCode(command.uldCode()), LocalDateTime.now(ZoneOffset.UTC));
        return save(load);
    }

    private Load required(FlightId id) {
        return loadPort.load(id)
                .orElseThrow(() -> new LoadNotFoundException("No load for flight " + id.key()));
    }

    private LoadFacts save(Load load) {
        updatePort.update(load);
        load.getDomainEvents().forEach(eventPort::publish);
        load.clearDomainEvents();
        return load.facts();
    }

}
