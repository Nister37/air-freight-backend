package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.loadplanning.port.in.command.CargoFeedCommand;
import java.time.*;
import java.util.*;

public interface ReceiveCargoFeedUseCase {
    void receiveCargoFeed(CargoFeedCommand command);
}
