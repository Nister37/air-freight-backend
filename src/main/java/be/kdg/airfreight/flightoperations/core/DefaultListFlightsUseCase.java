package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.domain.*;
import be.kdg.airfreight.flightoperations.port.in.usecase.ListFlightsUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DefaultListFlightsUseCase implements ListFlightsUseCase {
    private final FlightLoadPort loadPort;

    DefaultListFlightsUseCase(FlightLoadPort loadPort) {
        this.loadPort = loadPort;
    }

    @Override
    public List<FlightFacts> listFlights(LocalDate scheduledDepartureDate) {
        return loadPort.loadAll().stream().map(Flight::facts)
                .filter(f -> scheduledDepartureDate == null || f.id().scheduledDepartureDate().equals(scheduledDepartureDate))
                .sorted(Comparator.comparing(FlightFacts::scheduledDeparture)).toList();
    }
}
