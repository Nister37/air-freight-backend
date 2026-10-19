package be.kdg.airfreight.flightoperations.port.in.command;

import be.kdg.airfreight.common.FlightId;
import java.util.UUID;
import java.util.Objects;

public record RemoveDelayCommand(FlightId flightId, UUID delayId) {
    public RemoveDelayCommand {
        Objects.requireNonNull(flightId, "Flight ID is required");
        Objects.requireNonNull(delayId, "Delay ID is required");
    }
}
