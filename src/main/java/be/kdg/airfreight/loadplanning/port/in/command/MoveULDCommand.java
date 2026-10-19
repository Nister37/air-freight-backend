package be.kdg.airfreight.loadplanning.port.in.command;

import be.kdg.airfreight.common.FlightId;
import java.util.Objects;

public record MoveULDCommand(FlightId flightId, String uldCode, String position) {
    public MoveULDCommand {
        Objects.requireNonNull(flightId, "Flight ID is required");
        Objects.requireNonNull(uldCode, "ULD code is required");
        Objects.requireNonNull(position, "Position is required");
    }
}
