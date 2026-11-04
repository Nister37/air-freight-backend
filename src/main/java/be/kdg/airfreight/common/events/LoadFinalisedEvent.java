package be.kdg.airfreight.common.events;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.messages.PlacedUld;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public record LoadFinalisedEvent(UUID eventUUID, LocalDateTime eventPit, FlightId flightId,
                                 List<PlacedUld> placedUlds) implements DomainEvent {
    public LoadFinalisedEvent(FlightId flightId, List<PlacedUld> placedUlds) {
        this(UUID.randomUUID(), LocalDateTime.now(ZoneOffset.UTC), flightId, placedUlds);
    }

    public LoadFinalisedEvent {
        Objects.requireNonNull(eventUUID);
        Objects.requireNonNull(eventPit);
        Objects.requireNonNull(flightId);
        placedUlds = List.copyOf(placedUlds);
        if (placedUlds.stream().map(PlacedUld::uldCode).distinct().count() != placedUlds.size()
                || placedUlds.stream().map(PlacedUld::position).distinct().count() != placedUlds.size()) {
            throw new IllegalArgumentException("Final load must contain unique ULDs and positions");
        }
    }
}
