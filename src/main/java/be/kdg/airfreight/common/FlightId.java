package be.kdg.airfreight.common;

import java.time.LocalDate;
import java.util.Objects;

public record FlightId(FlightNumber flightNumber, LocalDate scheduledDepartureDate, Airport departureAirport) {
    public FlightId {
        Objects.requireNonNull(flightNumber, "Flight number is required");
        Objects.requireNonNull(scheduledDepartureDate, "Scheduled departure date is required");
        Objects.requireNonNull(departureAirport, "Departure airport is required");
    }

    public String key() {
        return flightNumber.value() + "/" + scheduledDepartureDate + "/" + departureAirport.value();
    }
}
