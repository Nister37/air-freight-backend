package be.kdg.airfreight.flightoperations.port.out;

import be.kdg.airfreight.flightoperations.domain.vo.FlightPolicy;

public interface FlightPolicyUpdatePort {
    void update(FlightPolicy policy);
}
