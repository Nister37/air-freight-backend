package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.FlightNotFoundException;
import be.kdg.airfreight.flightoperations.domain.*;
import be.kdg.airfreight.flightoperations.port.in.usecase.ProcessCPMUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.time.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultProcessCPMUseCase implements ProcessCPMUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProcessCPMUseCase.class);
    private final FlightLoadPort loadPort;
    private final FlightUpdatePort updatePort;
    private final FlightEventPort eventPort;

    DefaultProcessCPMUseCase(FlightLoadPort loadPort, FlightUpdatePort updatePort, FlightEventPort eventPort) {
        this.loadPort = loadPort;
        this.updatePort = updatePort;
        this.eventPort = eventPort;
    }

    @Override
    public void processCPM(FlightId id) {
        Flight flight = required(id);
        if (flight.facts().finalLoadReceived()) {
            LOGGER.info("Duplicate final-load CPM ignored for {}", id.key());
            return;
        }
        flight.confirmFinalLoadReceived();
        save(flight);
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
