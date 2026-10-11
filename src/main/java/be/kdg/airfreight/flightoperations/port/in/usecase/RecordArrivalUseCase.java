package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.port.in.command.RecordActualTimeCommand;
import java.time.*;
import java.util.*;

public interface RecordArrivalUseCase {
    FlightFacts recordArrival(RecordActualTimeCommand command);
}
