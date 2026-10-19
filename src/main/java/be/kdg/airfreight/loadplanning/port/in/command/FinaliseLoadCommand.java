package be.kdg.airfreight.loadplanning.port.in.command;

import be.kdg.airfreight.common.FlightId;
import java.util.Objects;

public record FinaliseLoadCommand(FlightId flightId, boolean confirmWarnings) {
    public FinaliseLoadCommand {
        Objects.requireNonNull(flightId, "Flight ID is required");
    }
}
