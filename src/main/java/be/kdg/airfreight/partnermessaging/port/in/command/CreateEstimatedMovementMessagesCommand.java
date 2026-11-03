package be.kdg.airfreight.partnermessaging.port.in.command;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record CreateEstimatedMovementMessagesCommand(UUID sourceEventId, LocalDateTime producedAt, FlightId flightId,
                                                     Airport arrivalAirport, AircraftRegistration aircraftRegistration,
                                                     LocalDateTime estimatedDeparture, LocalDateTime estimatedArrival,
                                                     boolean departureChanged, boolean arrivalChanged) {
    public CreateEstimatedMovementMessagesCommand {
        Objects.requireNonNull(sourceEventId, "Source event ID is required");
        Objects.requireNonNull(producedAt, "Produced-at UTC date and time is required");
        Objects.requireNonNull(flightId, "Flight ID is required");
        Objects.requireNonNull(arrivalAirport, "Arrival airport is required");
        Objects.requireNonNull(aircraftRegistration, "Aircraft registration is required");
        Objects.requireNonNull(estimatedDeparture, "Estimated departure is required");
        Objects.requireNonNull(estimatedArrival, "Estimated arrival is required");
    }
}
