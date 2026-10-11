package be.kdg.airfreight.common.events;

import be.kdg.airfreight.common.FlightFacts;
import java.time.LocalDateTime;
import java.util.UUID;

public record FlightDepartedEvent(UUID eventUUID, LocalDateTime eventPit, FlightFacts flight) implements DomainEvent { }
