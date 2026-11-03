package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.loadplanning.domain.LoadFacts;
import be.kdg.airfreight.loadplanning.port.in.command.MoveULDCommand;

public interface MoveULDUseCase {
    LoadFacts moveULD(MoveULDCommand command);
}
