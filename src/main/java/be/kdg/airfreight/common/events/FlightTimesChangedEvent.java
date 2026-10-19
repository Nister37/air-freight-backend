package be.kdg.airfreight.common.events;

import be.kdg.airfreight.common.FlightFacts;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record FlightTimesChangedEvent(UUID eventUUID, LocalDateTime eventPit, FlightFacts flight,
                                      boolean departureChanged, boolean arrivalChanged) implements DomainEvent {
    public FlightTimesChangedEvent(FlightFacts flight, boolean departureChanged, boolean arrivalChanged) {
        this(UUID.randomUUID(), LocalDateTime.now(ZoneOffset.UTC), flight, departureChanged, arrivalChanged);
    }
}
