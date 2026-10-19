package be.kdg.airfreight.loadplanning.adapter.out.api;

import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.flightoperations.FlightNotFoundException;
import be.kdg.airfreight.flightoperations.FlightOperationsApi;
import be.kdg.airfreight.loadplanning.port.out.FlightRequestPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class FlightFactsAdapter implements FlightRequestPort {
    private final FlightOperationsApi flights;

    FlightFactsAdapter(FlightOperationsApi flights) { this.flights = flights; }

    @Override
    public Optional<FlightFacts> findFlightFacts(FlightId id) {
        if (!flights.flightExists(id)) return Optional.empty();
        try {
            return Optional.of(flights.getFlightFacts(id));
        } catch (FlightNotFoundException exception) {
            return Optional.empty();
        }
    }
}
