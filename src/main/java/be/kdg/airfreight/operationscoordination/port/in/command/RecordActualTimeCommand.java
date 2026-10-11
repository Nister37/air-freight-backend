package be.kdg.airfreight.operationscoordination.port.in.command;

import be.kdg.airfreight.common.FlightId;
import java.time.LocalDateTime;

public record RecordActualTimeCommand(FlightId flightId, LocalDateTime actualTime) { }
