package be.kdg.airfreight.partnermessaging.port.in.command;

import be.kdg.airfreight.common.FlightFacts;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record CreateEstimatedMovementMessagesCommand(UUID sourceEventId, LocalDateTime producedAt, FlightFacts flight,
                                                     boolean departureChanged, boolean arrivalChanged) {
    public CreateEstimatedMovementMessagesCommand {
        Objects.requireNonNull(sourceEventId, "Source event ID is required");
        Objects.requireNonNull(producedAt, "Produced-at UTC date and time is required");
        Objects.requireNonNull(flight, "Flight facts are required");
    }
}
