package be.kdg.airfreight.operationscoordination.port.in.usecase;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.CorrectDelayCommand;
import java.time.*;
import java.util.*;

public interface CorrectFlightDelayUseCase {
    FlightFacts correctDelay(CorrectDelayCommand command);
}
