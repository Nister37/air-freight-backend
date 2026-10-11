package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.FlightNotFoundException;
import be.kdg.airfreight.flightoperations.domain.*;
import be.kdg.airfreight.flightoperations.port.in.command.RegisterDelayCommand;
import be.kdg.airfreight.flightoperations.port.in.usecase.RegisterDelayUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.time.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultRegisterDelayUseCase implements RegisterDelayUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRegisterDelayUseCase.class);
    private final FlightLoadPort loadPort;
    private final FlightUpdatePort updatePort;
    private final FlightEventPort eventPort;

    DefaultRegisterDelayUseCase(FlightLoadPort loadPort, FlightUpdatePort updatePort, FlightEventPort eventPort) {
        this.loadPort = loadPort;
        this.updatePort = updatePort;
        this.eventPort = eventPort;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_flight-planner')")
    public FlightFacts registerDelay(RegisterDelayCommand command) {
        Flight flight = required(command.flightId());
        Delay delay = new Delay(DelayId.create(), new DelayCode(command.code()), command.description(), command.minutes());
        if (command.phase() == DelayPhase.DEPARTURE) flight.registerDepartureDelay(delay, LocalDateTime.now(ZoneOffset.UTC));
        else if (command.phase() == DelayPhase.ARRIVAL) flight.registerArrivalDelay(delay, LocalDateTime.now(ZoneOffset.UTC));
        else throw new IllegalArgumentException("Delay phase is required");
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
