package be.kdg.airfreight.flightoperations.port.in.command;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.domain.enums.DelayPhase;
import java.util.Objects;

public record RegisterDelayCommand(FlightId flightId, DelayPhase phase, String code, int minutes, String description) {
    public RegisterDelayCommand {
        Objects.requireNonNull(flightId, "Flight ID is required");
        Objects.requireNonNull(phase, "Delay phase is required");
        Objects.requireNonNull(code, "Delay code is required");
        Objects.requireNonNull(description, "Delay description is required");
    }
}
