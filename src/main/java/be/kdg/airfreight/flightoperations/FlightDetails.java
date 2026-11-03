package be.kdg.airfreight.flightoperations;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;
import java.time.LocalDateTime;
import java.util.Objects;

/** Public flight information needed to plan a load. */
public record FlightDetails(FlightId id, Airport arrivalAirport, AircraftRegistration aircraftRegistration,
                            LocalDateTime scheduledDeparture, LocalDateTime estimatedDeparture) {
    public FlightDetails {
        Objects.requireNonNull(id);
        Objects.requireNonNull(arrivalAirport);
        Objects.requireNonNull(aircraftRegistration);
        Objects.requireNonNull(scheduledDeparture);
        Objects.requireNonNull(estimatedDeparture);
    }
}
