package be.kdg.airfreight.common.events;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record FlightLandedEvent(UUID eventUUID, LocalDateTime eventPit, FlightId flightId, Airport arrivalAirport,
                                 AircraftRegistration aircraftRegistration, LocalDateTime actualArrival) implements DomainEvent {
    public FlightLandedEvent(FlightId flightId, Airport arrivalAirport, AircraftRegistration aircraftRegistration, LocalDateTime actualArrival) {
        this(UUID.randomUUID(), LocalDateTime.now(ZoneOffset.UTC), flightId, arrivalAirport, aircraftRegistration, actualArrival);
    }
}
