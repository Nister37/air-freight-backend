package be.kdg.airfreight.common.events;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.FlightId;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record FlightDepartedEvent(UUID eventUUID, LocalDateTime eventPit, FlightId flightId,
                                   AircraftRegistration aircraftRegistration, LocalDateTime actualDeparture) implements DomainEvent {
    public FlightDepartedEvent(FlightId flightId, AircraftRegistration aircraftRegistration, LocalDateTime actualDeparture) {
        this(UUID.randomUUID(), LocalDateTime.now(ZoneOffset.UTC), flightId, aircraftRegistration, actualDeparture);
    }
}
