package be.kdg.airfreight.loadplanning.port.in.command;

import be.kdg.airfreight.common.FlightId;
import java.util.Objects;

public record PlaceULDCommand(FlightId flightId, String uldCode, String position) {
    public PlaceULDCommand {
        Objects.requireNonNull(flightId, "Flight ID is required");
        Objects.requireNonNull(uldCode, "ULD code is required");
        Objects.requireNonNull(position, "Position is required");
    }
}
