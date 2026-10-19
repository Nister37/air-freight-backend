package be.kdg.airfreight.flightoperations.port.in.command;

import be.kdg.airfreight.common.FlightId;
import java.util.UUID;
import java.util.Objects;

public record CorrectDelayCommand(FlightId flightId, UUID delayId, int minutes, String description) {
    public CorrectDelayCommand {
        Objects.requireNonNull(flightId, "Flight ID is required");
        Objects.requireNonNull(delayId, "Delay ID is required");
        Objects.requireNonNull(description, "Delay description is required");
    }
}
