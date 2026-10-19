package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.domain.Flight;
import be.kdg.airfreight.flightoperations.port.in.usecase.ListFlightsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightSelection;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DefaultListFlightsQuery implements ListFlightsQuery {
    private final FlightLoadPort loadPort;

    DefaultListFlightsQuery(FlightLoadPort loadPort) {
        this.loadPort = loadPort;
    }

    @Override
    public List<FlightFacts> listFlights(LocalDate scheduledDepartureDate) {
        return loadPort.findFlights(new FlightSelection(scheduledDepartureDate, null, null, null, null))
                .stream().map(Flight::facts).toList();
    }
}
