package be.kdg.airfreight.operationscoordination.port.in.usecase;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.RemoveDelayCommand;
import java.time.*;
import java.util.*;

public interface RemoveFlightDelayUseCase {
    FlightFacts removeDelay(RemoveDelayCommand command);
}
