package be.kdg.airfreight.flightoperations.port.in.command;

import be.kdg.airfreight.common.FlightId;
import java.util.UUID;

public record RemoveDelayCommand(FlightId flightId, UUID delayId) { }
