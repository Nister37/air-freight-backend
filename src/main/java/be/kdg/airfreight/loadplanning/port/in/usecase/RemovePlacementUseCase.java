package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.loadplanning.port.in.command.RemovePlacementCommand;

public interface RemovePlacementUseCase {
    LoadFacts removePlacement(RemovePlacementCommand command);
}
