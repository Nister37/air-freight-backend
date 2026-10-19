package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.loadplanning.domain.LoadPlan;

public interface GetLoadPlanQuery {
    LoadPlan getLoadPlan(FlightId id);
}
