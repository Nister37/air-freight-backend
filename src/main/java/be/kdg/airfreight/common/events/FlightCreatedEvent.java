package be.kdg.airfreight.common.events;

import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/** Flight Operations publishes it; Load Planning creates a load, and Partner Messaging creates an FLM. */
public record FlightCreatedEvent(UUID eventUUID, LocalDateTime eventPit, FlightId flightId, Airport arrivalAirport,
                                  LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) implements DomainEvent {
    public FlightCreatedEvent(FlightId flightId, Airport arrivalAirport, LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) {
        this(UUID.randomUUID(), LocalDateTime.now(ZoneOffset.UTC), flightId, arrivalAirport, scheduledDeparture, scheduledArrival);
    }
}
