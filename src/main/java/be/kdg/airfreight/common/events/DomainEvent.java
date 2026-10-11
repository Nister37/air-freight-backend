package be.kdg.airfreight.common.events;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {
    UUID eventUUID();
    LocalDateTime eventPit();
}
