package be.kdg.airfreight.operationscoordination.port.in.usecase;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.RecordActualTimeCommand;
import java.time.*;
import java.util.*;

public interface RecordFlightArrivalUseCase {
    FlightFacts recordArrival(RecordActualTimeCommand command);
}
