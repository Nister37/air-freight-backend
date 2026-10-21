package be.kdg.airfreight.flightoperations.port.in.command;

import be.kdg.airfreight.flightoperations.domain.FlightPolicy;
import java.util.Objects;

public record UpdateFlightPolicyCommand(FlightPolicy policy) {
    public UpdateFlightPolicyCommand {
        Objects.requireNonNull(policy, "Policy is required");
    }
}
