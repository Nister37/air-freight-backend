package be.kdg.airfreight.loadplanning.port.out;

import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.common.FlightId;
import java.util.Optional;

public interface FlightRequestPort {
    Optional<FlightFacts> findFlightFacts(FlightId id);
}
