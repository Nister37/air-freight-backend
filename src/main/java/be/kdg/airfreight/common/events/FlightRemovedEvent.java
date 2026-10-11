package be.kdg.airfreight.common.events;

import be.kdg.airfreight.common.FlightId;
import java.time.LocalDateTime;
import java.util.UUID;

public record FlightRemovedEvent(UUID eventUUID, LocalDateTime eventPit, FlightId flightId) implements DomainEvent { }
