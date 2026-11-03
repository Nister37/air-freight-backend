package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.port.in.usecase.GetFlightQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DefaultGetFlightQuery implements GetFlightQuery {
    private final FlightLookup flightLookup;

    DefaultGetFlightQuery(FlightLookup flightLookup) {
        this.flightLookup = flightLookup;
    }

    @Override
    public Flight getFlight(FlightId id) {
        return flightLookup.required(id);
    }

}
