package be.kdg.airfreight.partnermessaging.port.in.command;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.PlacedUld;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CreateCargoPositionMessageCommand(UUID sourceEventId, LocalDateTime producedAt, FlightId flightId,
                                                List<PlacedUld> placedUlds) {
    public CreateCargoPositionMessageCommand {
        Objects.requireNonNull(sourceEventId, "Source event ID is required");
        Objects.requireNonNull(producedAt, "Produced-at UTC date and time is required");
        Objects.requireNonNull(flightId, "Flight ID is required");
        placedUlds = List.copyOf(placedUlds);
        if (placedUlds.stream().map(PlacedUld::uldCode).distinct().count() != placedUlds.size()
                || placedUlds.stream().map(PlacedUld::position).distinct().count() != placedUlds.size()) {
            throw new IllegalArgumentException("Final load must contain unique ULDs and positions");
        }
    }
}
