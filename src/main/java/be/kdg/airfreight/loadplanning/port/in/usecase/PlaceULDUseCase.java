package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.loadplanning.port.in.command.PlaceULDCommand;

public interface PlaceULDUseCase {
    LoadFacts placeULD(PlaceULDCommand command);
}
