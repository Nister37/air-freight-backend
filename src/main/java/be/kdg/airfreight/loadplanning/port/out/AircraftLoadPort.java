package be.kdg.airfreight.loadplanning.port.out;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.loadplanning.domain.vo.FloorPlan;

import java.util.Optional;

public interface AircraftLoadPort {
    Optional<FloorPlan> floorPlanFor(AircraftRegistration registration);
}
