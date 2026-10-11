package be.kdg.airfreight.common;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/** Immutable public facts; the Flight aggregate never leaves Flight Operations. */
public record FlightFacts(FlightId id, Airport arrivalAirport, AircraftRegistration aircraftRegistration,
                          LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival,
                          LocalDateTime estimatedDeparture, LocalDateTime estimatedArrival,
                          LocalDateTime actualDeparture, LocalDateTime actualArrival,
                          FlightStatus status, boolean finalLoadReceived, List<DelayDetails> delays) {
    public FlightFacts {
        Objects.requireNonNull(id);
        Objects.requireNonNull(arrivalAirport);
        Objects.requireNonNull(aircraftRegistration);
        Objects.requireNonNull(scheduledDeparture);
        Objects.requireNonNull(scheduledArrival);
        Objects.requireNonNull(estimatedDeparture);
        Objects.requireNonNull(estimatedArrival);
        Objects.requireNonNull(status);
        delays = List.copyOf(delays);
    }
}
