package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.loadplanning.port.in.command.FinaliseLoadCommand;

public interface FinaliseLoadUseCase {
    LoadFacts finaliseLoad(FinaliseLoadCommand command);
}
