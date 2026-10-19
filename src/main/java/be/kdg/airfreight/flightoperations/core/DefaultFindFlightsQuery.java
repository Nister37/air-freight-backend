package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.flightoperations.domain.Flight;
import be.kdg.airfreight.flightoperations.port.in.usecase.FindFlightsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightSelection;
import be.kdg.airfreight.flightoperations.port.out.FlightLoadPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional(readOnly = true)
class DefaultFindFlightsQuery implements FindFlightsQuery {
    private final FlightLoadPort loadPort;

    DefaultFindFlightsQuery(FlightLoadPort loadPort) {
        this.loadPort = loadPort;
    }

    @Override
    public List<FlightFacts> findFlights(FlightSelection selection) {
        LocalDate date = selection.date() == null ? LocalDate.now(ZoneOffset.UTC) : selection.date();
        return loadPort.findFlights(new FlightSelection(date, selection.number(), selection.departureAirport(),
                selection.arrivalAirport(), selection.status())).stream().map(Flight::facts).toList();
    }
}
