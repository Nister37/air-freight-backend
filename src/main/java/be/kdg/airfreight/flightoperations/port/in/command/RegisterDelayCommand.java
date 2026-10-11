package be.kdg.airfreight.flightoperations.port.in.command;

import be.kdg.airfreight.common.*;

public record RegisterDelayCommand(FlightId flightId, DelayPhase phase, String code, int minutes, String description) { }
