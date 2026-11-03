package be.kdg.airfreight.flightoperations.port.out;

import be.kdg.airfreight.flightoperations.domain.vo.FlightPolicy;
import java.util.Optional;

public interface FlightPolicyLoadPort {
    Optional<FlightPolicy> load();
}
