package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.flightoperations.port.in.usecase.FindFlightsQuery;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightSelection;
import be.kdg.airfreight.flightoperations.port.out.FlightLoadPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
class DefaultFindFlightsQuery implements FindFlightsQuery {
    private final FlightLoadPort loadPort;

    DefaultFindFlightsQuery(FlightLoadPort loadPort) {
        this.loadPort = loadPort;
    }

    @Override
    public List<Flight> findFlights(FlightSelection selection) {
        return loadPort.findFlights(selection);
    }
}
