package be.kdg.airfreight.loadplanning;

import be.kdg.airfreight.common.CargoBooking;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.LoadFacts;

/** Integration contract only. The Load contributor supplies its implementation and booking rules. */
public interface LoadPlanningApi {
    void bookCargo(CargoBooking booking);
    LoadFacts getLoad(FlightId id);
    LoadFacts placeULD(FlightId id, String uldCode, String position);
    LoadFacts removePlacement(FlightId id, String uldCode);
    LoadFacts checkLoad(FlightId id);
    LoadFacts finaliseLoad(FlightId id, boolean confirmWarnings);
}
