package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.port.in.command.RemoveDelayCommand;
import java.time.*;
import java.util.*;

public interface RemoveDelayUseCase {
    FlightFacts removeDelay(RemoveDelayCommand command);
}
