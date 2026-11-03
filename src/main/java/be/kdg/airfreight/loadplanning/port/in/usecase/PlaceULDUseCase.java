package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.loadplanning.domain.LoadFacts;
import be.kdg.airfreight.loadplanning.port.in.command.PlaceULDCommand;

public interface PlaceULDUseCase {
    LoadFacts placeULD(PlaceULDCommand command);
}
