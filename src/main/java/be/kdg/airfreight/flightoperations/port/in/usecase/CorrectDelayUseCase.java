package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.port.in.command.CorrectDelayCommand;
import java.time.*;
import java.util.*;

public interface CorrectDelayUseCase {
    FlightFacts correctDelay(CorrectDelayCommand command);
}
