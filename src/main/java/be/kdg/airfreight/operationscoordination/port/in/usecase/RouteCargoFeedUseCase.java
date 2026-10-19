package be.kdg.airfreight.operationscoordination.port.in.usecase;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.CargoFeedCommand;
import java.time.*;
import java.util.*;

public interface RouteCargoFeedUseCase {
    void routeCargoFeed(CargoFeedCommand command);
}
