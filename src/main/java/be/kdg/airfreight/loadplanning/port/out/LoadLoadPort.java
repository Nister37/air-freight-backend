package be.kdg.airfreight.loadplanning.port.out;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.loadplanning.domain.Load;

import java.util.Optional;

public interface LoadLoadPort {
    Optional<Load> load(FlightId id);
}
