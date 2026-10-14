package be.kdg.airfreight.loadplanning.port.out;

import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.common.FlightId;

public interface FlightRequestPort {
    FlightFacts getFlightFacts(FlightId id);
}
