package be.kdg.airfreight.partnermessaging.port.in.command;

import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record CreateFlightListMessageCommand(UUID sourceEventId, LocalDateTime producedAt, FlightId flightId,
                                             Airport arrivalAirport, LocalDateTime scheduledDeparture,
                                             LocalDateTime scheduledArrival) {
    public CreateFlightListMessageCommand {
        Objects.requireNonNull(sourceEventId, "Source event ID is required");
        Objects.requireNonNull(producedAt, "Produced-at UTC date and time is required");
        Objects.requireNonNull(flightId, "Flight ID is required");
        Objects.requireNonNull(arrivalAirport, "Arrival airport is required");
        Objects.requireNonNull(scheduledDeparture, "Scheduled departure is required");
        Objects.requireNonNull(scheduledArrival, "Scheduled arrival is required");
    }
}
