package be.kdg.airfreight.common.events;

import be.kdg.airfreight.common.FlightFacts;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record FlightCreatedEvent(UUID eventUUID, LocalDateTime eventPit, FlightFacts flight) implements DomainEvent {
    public FlightCreatedEvent(FlightFacts flight) {
        this(UUID.randomUUID(), LocalDateTime.now(ZoneOffset.UTC), flight);
    }
}
