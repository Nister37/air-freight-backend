package be.kdg.airfreight.partnermessaging.port.in.command;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record CreateActualMovementMessageCommand(UUID sourceEventId, LocalDateTime producedAt, FlightId flightId,
                                                 AircraftRegistration aircraftRegistration, Airport airport,
                                                 LocalDateTime movementTime) {
    public CreateActualMovementMessageCommand {
        Objects.requireNonNull(sourceEventId, "Source event ID is required");
        Objects.requireNonNull(producedAt, "Produced-at UTC date and time is required");
        Objects.requireNonNull(flightId, "Flight ID is required");
        Objects.requireNonNull(aircraftRegistration, "Aircraft registration is required");
        Objects.requireNonNull(airport, "Movement airport is required");
        Objects.requireNonNull(movementTime, "Movement time is required");
    }
}
