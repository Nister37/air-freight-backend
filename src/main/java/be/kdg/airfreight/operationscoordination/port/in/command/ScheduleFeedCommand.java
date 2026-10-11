package be.kdg.airfreight.operationscoordination.port.in.command;

import be.kdg.airfreight.common.FlightSchedule;
import java.util.*;

public record ScheduleFeedCommand(UUID messageId, FlightSchedule schedule) {
    public ScheduleFeedCommand {
        Objects.requireNonNull(messageId, "SSM message ID is required");
        Objects.requireNonNull(schedule);
    }
}
