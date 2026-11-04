package be.kdg.airfreight.common.events;

import be.kdg.airfreight.common.messages.MessageKind;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record PartnerMessageCreatedEvent(UUID eventUUID, LocalDateTime eventPit, int formatVersion,
                                         MessageKind kind, String body) implements DomainEvent {
    public PartnerMessageCreatedEvent {
        Objects.requireNonNull(eventUUID);
        Objects.requireNonNull(eventPit);
        Objects.requireNonNull(kind);
        Objects.requireNonNull(body);
    }
}
