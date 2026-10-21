package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.flightoperations.domain.FlightPolicy;
import be.kdg.airfreight.flightoperations.port.out.FlightPolicyLoadPort;
import org.springframework.stereotype.Component;

@Component
class FlightPolicyReader {
    private final FlightPolicyLoadPort loadPort;

    FlightPolicyReader(FlightPolicyLoadPort loadPort) {
        this.loadPort = loadPort;
    }

    FlightPolicy current() {
        return loadPort.load().orElseGet(FlightPolicy::defaults);
    }
}
