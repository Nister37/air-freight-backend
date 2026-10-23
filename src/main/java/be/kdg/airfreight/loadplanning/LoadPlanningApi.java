package be.kdg.airfreight.loadplanning;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.LoadFacts;

/** Public entry points for load placement and finalisation. */
public interface LoadPlanningApi {
    LoadFacts placeULD(FlightId id, String uldCode, String position);
    LoadFacts finaliseLoad(FlightId id, boolean confirmWarnings);
}
