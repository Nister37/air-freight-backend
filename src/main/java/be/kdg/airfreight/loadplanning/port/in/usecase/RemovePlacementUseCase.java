package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.loadplanning.domain.LoadFacts;
import be.kdg.airfreight.loadplanning.port.in.command.RemovePlacementCommand;

public interface RemovePlacementUseCase {
    LoadFacts removePlacement(RemovePlacementCommand command);
}
