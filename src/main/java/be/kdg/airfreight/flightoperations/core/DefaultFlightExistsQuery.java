package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightExistsQuery;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DefaultFlightExistsQuery implements FlightExistsQuery {
    private final FlightLoadPort loadPort;

    DefaultFlightExistsQuery(FlightLoadPort loadPort) {
        this.loadPort = loadPort;
    }

    @Override
    public boolean flightExists(FlightId id) {
        return loadPort.load(id).isPresent();
    }
}
