package be.kdg.airfreight.flightoperations.port.out;

import be.kdg.airfreight.flightoperations.domain.FlightPolicy;

public interface FlightPolicyUpdatePort {
    void update(FlightPolicy policy);
}
