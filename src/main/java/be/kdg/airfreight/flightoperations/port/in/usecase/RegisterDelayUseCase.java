package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.port.in.command.RegisterDelayCommand;
import java.time.*;
import java.util.*;

public interface RegisterDelayUseCase {
    FlightFacts registerDelay(RegisterDelayCommand command);
}
