package be.kdg.airfreight.common.events;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

/** A stored delay change containing values rather than Flight Operations' internal objects. */
public record DelayEvent(UUID eventUUID, LocalDateTime eventPit, String type, String phase,
                         UUID delayId, String code, String description, int minutes) implements DomainEvent {
    public DelayEvent {
        Objects.requireNonNull(eventUUID);
        Objects.requireNonNull(eventPit);
        Objects.requireNonNull(delayId);
    }

    public DelayEvent(String type, String phase, UUID delayId, String code, String description, int minutes) {
        this(UUID.randomUUID(), LocalDateTime.now(ZoneOffset.UTC), type, phase, delayId, code, description, minutes);
    }
}
