package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.port.in.usecase.GetFlightFactsUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DefaultGetFlightFactsUseCase implements GetFlightFactsUseCase {
    private final FlightLookup flightLookup;

    DefaultGetFlightFactsUseCase(FlightLookup flightLookup) {
        this.flightLookup = flightLookup;
    }

    @Override
    public FlightFacts getFlightFacts(FlightId id) {
        return flightLookup.required(id).facts();
    }

}
