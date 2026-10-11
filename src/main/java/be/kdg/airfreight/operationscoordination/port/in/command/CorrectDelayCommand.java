package be.kdg.airfreight.operationscoordination.port.in.command;

import be.kdg.airfreight.common.FlightId;
import java.util.UUID;

public record CorrectDelayCommand(FlightId flightId, UUID delayId, int minutes, String description) { }
