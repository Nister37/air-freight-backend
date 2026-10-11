package be.kdg.airfreight.loadplanning;

import be.kdg.airfreight.common.CargoBooking;

/** Integration contract only. The Load contributor supplies its implementation and booking rules. */
public interface LoadPlanningApi {
    void bookCargo(CargoBooking booking);
}
