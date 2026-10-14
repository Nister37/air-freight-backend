package be.kdg.airfreight.common;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/** Immutable public facts; the Load aggregate never leaves Load Planning. */
public record LoadFacts(FlightId flightId, AircraftRegistration aircraftRegistration, Airport destination,
                        LocalDateTime loadingWindowOpensAt, LocalDateTime loadingWindowClosesAt,
                        boolean finalised, List<UldDetails> ulds, List<LoadCheckDetails> checks) {
    public LoadFacts {
        Objects.requireNonNull(flightId);
        Objects.requireNonNull(aircraftRegistration);
        Objects.requireNonNull(destination);
        Objects.requireNonNull(loadingWindowOpensAt);
        Objects.requireNonNull(loadingWindowClosesAt);
        ulds = List.copyOf(ulds);
        checks = List.copyOf(checks);
    }
}
