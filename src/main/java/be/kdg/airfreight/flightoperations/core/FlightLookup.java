package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.flightoperations.FlightNotFoundException;
import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.flightoperations.port.out.FlightLoadPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class FlightLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightLookup.class);
    private final FlightLoadPort loadPort;

    FlightLookup(FlightLoadPort loadPort) {
        this.loadPort = loadPort;
    }

    Flight required(FlightId id) {
        return loadPort.load(id).orElseThrow(() -> {
            LOGGER.warn("Flight does not exist: {}", id.key());
            return new FlightNotFoundException("Flight does not exist: " + id.key());
        });
    }
}
