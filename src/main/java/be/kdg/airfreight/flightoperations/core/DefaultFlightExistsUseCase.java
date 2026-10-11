package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.domain.*;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightExistsUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DefaultFlightExistsUseCase implements FlightExistsUseCase {
    private final FlightLoadPort loadPort;

    DefaultFlightExistsUseCase(FlightLoadPort loadPort) {
        this.loadPort = loadPort;
    }

    @Override
    public boolean flightExists(FlightId id) {
        return loadPort.load(id).isPresent();
    }
}
