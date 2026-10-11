package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.FlightNotFoundException;
import be.kdg.airfreight.flightoperations.domain.*;
import be.kdg.airfreight.flightoperations.port.in.command.RecordActualTimeCommand;
import be.kdg.airfreight.flightoperations.port.in.usecase.RecordArrivalUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.time.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultRecordArrivalUseCase implements RecordArrivalUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRecordArrivalUseCase.class);
    private final FlightLoadPort loadPort;
    private final FlightUpdatePort updatePort;
    private final FlightEventPort eventPort;
    private final PolicyQuery policyQuery;

    DefaultRecordArrivalUseCase(FlightLoadPort loadPort, FlightUpdatePort updatePort, FlightEventPort eventPort, PolicyQuery policyQuery) {
        this.loadPort = loadPort;
        this.updatePort = updatePort;
        this.eventPort = eventPort;
        this.policyQuery = policyQuery;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_flight-planner')")
    public FlightFacts recordArrival(RecordActualTimeCommand command) {
        Flight flight = required(command.flightId());
        flight.recordArrival(command.actualTime(), LocalDateTime.now(ZoneOffset.UTC), policyQuery.currentPolicy().actualTimeToleranceMinutes());
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
