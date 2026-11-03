package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.flightoperations.port.in.command.CorrectDelayCommand;

public interface CorrectDelayUseCase {
    Flight correctDelay(CorrectDelayCommand command);
}
