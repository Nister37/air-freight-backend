package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.events.FlightRemovedEvent;
import be.kdg.airfreight.flightoperations.FlightNotFoundException;
import be.kdg.airfreight.flightoperations.domain.*;
import be.kdg.airfreight.flightoperations.port.in.usecase.RemoveExpiredFlightsUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.time.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultRemoveExpiredFlightsUseCase implements RemoveExpiredFlightsUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRemoveExpiredFlightsUseCase.class);
    private final FlightLoadPort loadPort;
    private final FlightUpdatePort updatePort;
    private final FlightEventPort eventPort;
    private final PolicyQuery policyQuery;

    DefaultRemoveExpiredFlightsUseCase(FlightLoadPort loadPort, FlightUpdatePort updatePort, FlightEventPort eventPort, PolicyQuery policyQuery) {
        this.loadPort = loadPort;
        this.updatePort = updatePort;
        this.eventPort = eventPort;
        this.policyQuery = policyQuery;
    }

    @Override
    public void removeExpiredFlights() {
        LocalDate cutoff = LocalDate.now(ZoneOffset.UTC).minusDays(policyQuery.currentPolicy().flightRetentionDays());
        for (Flight expired : loadPort.loadExpired(cutoff)) {
            Flight flight = required(expired.facts().id());
            updatePort.remove(flight);
            eventPort.publish(new FlightRemovedEvent(UUID.randomUUID(), LocalDateTime.now(ZoneOffset.UTC), flight.facts().id()));
        }
    }

    private Flight required(FlightId id) {
        return loadPort.load(id).orElseThrow(() -> {
            LOGGER.warn("Flight does not exist: {}", id.key());
            return new FlightNotFoundException("Flight does not exist: " + id.key());
        });
    }
}
