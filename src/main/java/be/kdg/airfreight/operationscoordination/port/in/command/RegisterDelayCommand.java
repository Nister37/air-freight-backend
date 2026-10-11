package be.kdg.airfreight.operationscoordination.port.in.command;

import be.kdg.airfreight.common.*;

public record RegisterDelayCommand(FlightId flightId, DelayPhase phase, String code, int minutes, String description) { }
