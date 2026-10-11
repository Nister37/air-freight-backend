package be.kdg.airfreight.partnermessaging.domain;

import be.kdg.airfreight.common.FlightId;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record PartnerMessage(UUID id, UUID sourceEventId, LocalDateTime producedAt, int formatVersion,
                             FlightId flightId, MessageKind kind, String body) {
    public PartnerMessage {
        Objects.requireNonNull(id);
        Objects.requireNonNull(sourceEventId);
        Objects.requireNonNull(producedAt);
        Objects.requireNonNull(flightId);
        Objects.requireNonNull(kind);
        Objects.requireNonNull(body);
    }
}
