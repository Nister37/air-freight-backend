package be.kdg.airfreight.loadplanning.port.in.command;

import be.kdg.airfreight.common.FlightId;
import java.util.Objects;

public record RemovePlacementCommand(FlightId flightId, String uldCode) {
    public RemovePlacementCommand {
        Objects.requireNonNull(flightId, "Flight ID is required");
        Objects.requireNonNull(uldCode, "ULD code is required");
    }
}
