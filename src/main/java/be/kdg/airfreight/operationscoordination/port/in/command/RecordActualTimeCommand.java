package be.kdg.airfreight.operationscoordination.port.in.command;

import be.kdg.airfreight.common.FlightId;
import java.time.LocalDateTime;
import java.util.Objects;

public record RecordActualTimeCommand(FlightId flightId, LocalDateTime actualTime) {
    public RecordActualTimeCommand {
        Objects.requireNonNull(flightId, "Flight ID is required");
        Objects.requireNonNull(actualTime, "Actual UTC date and time is required");
    }
}
