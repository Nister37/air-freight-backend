package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.loadplanning.domain.LoadFacts;

public interface GetLoadQuery {
    LoadFacts getLoad(FlightId id);
}
