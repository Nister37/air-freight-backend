package be.kdg.airfreight.loadplanning.port.out;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;
import java.time.LocalDateTime;
import java.util.Objects;

/** Load Planning's result of requesting the current flight information. */
public record FlightInfo(FlightId id, Airport destinationAirport, AircraftRegistration aircraftRegistration,
                         LocalDateTime scheduledDeparture, LocalDateTime estimatedDeparture) {
    public FlightInfo {
        Objects.requireNonNull(id);
        Objects.requireNonNull(destinationAirport);
        Objects.requireNonNull(aircraftRegistration);
        Objects.requireNonNull(scheduledDeparture);
        Objects.requireNonNull(estimatedDeparture);
    }
}
