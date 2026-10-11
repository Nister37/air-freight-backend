package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.FlightNotFoundException;
import be.kdg.airfreight.flightoperations.domain.*;
import be.kdg.airfreight.flightoperations.port.in.command.RemoveDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.usecase.RemoveDelayUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.time.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultRemoveDelayUseCase implements RemoveDelayUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRemoveDelayUseCase.class);
    private final FlightLoadPort loadPort;
    private final FlightUpdatePort updatePort;
    private final FlightEventPort eventPort;

    DefaultRemoveDelayUseCase(FlightLoadPort loadPort, FlightUpdatePort updatePort, FlightEventPort eventPort) {
        this.loadPort = loadPort;
        this.updatePort = updatePort;
        this.eventPort = eventPort;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_flight-planner')")
    public FlightFacts removeDelay(RemoveDelayCommand command) {
        Flight flight = required(command.flightId());
        flight.removeDelay(new DelayId(command.delayId()), LocalDateTime.now(ZoneOffset.UTC));
        return save(flight);
    }

    private Flight required(FlightId id) {
        return loadPort.load(id).orElseThrow(() -> {
            LOGGER.warn("Flight does not exist: {}", id.key());
            return new FlightNotFoundException("Flight does not exist: " + id.key());
        });
    }

    private FlightFacts save(Flight flight) {
        updatePort.update(flight);
        flight.getDomainEvents().forEach(eventPort::publish);
        flight.clearDomainEvents();
        return flight.facts();
    }
}
