package be.kdg.airfreight.common.events;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record FlightTimesChangedEvent(UUID eventUUID, LocalDateTime eventPit, FlightId flightId, Airport arrivalAirport,
                                       AircraftRegistration aircraftRegistration,
                                       LocalDateTime estimatedDeparture, LocalDateTime estimatedArrival,
                                       boolean departureChanged, boolean arrivalChanged) implements DomainEvent {
    public FlightTimesChangedEvent(FlightId flightId, Airport arrivalAirport, AircraftRegistration aircraftRegistration,
                                   LocalDateTime estimatedDeparture, LocalDateTime estimatedArrival,
                                   boolean departureChanged, boolean arrivalChanged) {
        this(UUID.randomUUID(), LocalDateTime.now(ZoneOffset.UTC), flightId, arrivalAirport, aircraftRegistration,
                estimatedDeparture, estimatedArrival, departureChanged, arrivalChanged);
    }
}
