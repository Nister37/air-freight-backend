package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.flightoperations.port.in.command.RegisterDelayCommand;

public interface RegisterDelayUseCase {
    Flight registerDelay(RegisterDelayCommand command);
}
