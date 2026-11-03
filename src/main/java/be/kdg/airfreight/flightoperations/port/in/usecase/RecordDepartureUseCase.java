package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.flightoperations.port.in.command.RecordActualTimeCommand;

public interface RecordDepartureUseCase {
    Flight recordDeparture(RecordActualTimeCommand command);
}
