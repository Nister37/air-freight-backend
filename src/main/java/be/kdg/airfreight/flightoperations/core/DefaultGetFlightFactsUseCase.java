package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.FlightNotFoundException;
import be.kdg.airfreight.flightoperations.domain.*;
import be.kdg.airfreight.flightoperations.port.in.usecase.GetFlightFactsUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.time.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DefaultGetFlightFactsUseCase implements GetFlightFactsUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGetFlightFactsUseCase.class);
    private final FlightLoadPort loadPort;

    DefaultGetFlightFactsUseCase(FlightLoadPort loadPort) {
        this.loadPort = loadPort;
    }

    @Override
    public FlightFacts getFlightFacts(FlightId id) {
        return required(id).facts();
    }

    private Flight required(FlightId id) {
        return loadPort.load(id).orElseThrow(() -> {
            LOGGER.warn("Flight does not exist: {}", id.key());
            return new FlightNotFoundException("Flight does not exist: " + id.key());
        });
    }
}
