package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
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
    private final FlightLookup flightLookup;
    private final List<FlightUpdatePort> updatePorts;

    DefaultProcessCPMUseCase(FlightLookup flightLookup, List<FlightUpdatePort> updatePorts) {
        this.flightLookup = flightLookup;
        this.updatePorts = List.copyOf(updatePorts);
    }

    @Override
    public void processCPM(FlightId id) {
        Flight flight = flightLookup.required(id);
        if (flight.facts().finalLoadReceived()) {
            LOGGER.info("Duplicate final-load CPM ignored for {}", id.key());
            return;
        }
        flight.confirmFinalLoadReceived();
        save(flight);
    }

    private FlightFacts save(Flight flight) {
        updatePorts.forEach(port -> port.update(flight));
        return flight.facts();
    }
}
