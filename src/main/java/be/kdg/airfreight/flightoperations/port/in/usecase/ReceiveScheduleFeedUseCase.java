package be.kdg.airfreight.flightoperations.port.in.usecase;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.port.in.command.ScheduleFeedCommand;
import java.time.*;
import java.util.*;

public interface ReceiveScheduleFeedUseCase {
    void receiveScheduleFeed(ScheduleFeedCommand command);
}
