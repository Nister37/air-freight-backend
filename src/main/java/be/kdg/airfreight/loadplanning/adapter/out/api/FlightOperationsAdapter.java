package be.kdg.airfreight.loadplanning.adapter.out.api;

import be.kdg.airfreight.loadplanning.port.out.FlightInfo;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.flightoperations.FlightNotFoundException;
import be.kdg.airfreight.flightoperations.FlightOperationsApi;
import be.kdg.airfreight.loadplanning.port.out.FlightRequestPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class FlightOperationsAdapter implements FlightRequestPort {
    private final FlightOperationsApi flights;

    FlightOperationsAdapter(FlightOperationsApi flights) { this.flights = flights; }

    @Override
    public Optional<FlightInfo> findFlight(FlightId id) {
        if (!flights.flightExists(id)) return Optional.empty();
        try {
            var flight = flights.getFlightDetails(id);
            return Optional.of(new FlightInfo(flight.id(), flight.arrivalAirport(), flight.aircraftRegistration(),
                    flight.scheduledDeparture(), flight.estimatedDeparture()));
        } catch (FlightNotFoundException exception) {
            return Optional.empty();
        }
    }
}
