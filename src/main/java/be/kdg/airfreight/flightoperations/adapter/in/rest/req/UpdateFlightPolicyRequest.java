package be.kdg.airfreight.flightoperations.adapter.in.rest.req;

import be.kdg.airfreight.flightoperations.domain.vo.FlightPolicy;
import be.kdg.airfreight.flightoperations.port.in.command.UpdateFlightPolicyCommand;

public record UpdateFlightPolicyRequest(Integer flightRetentionDays, Integer actualTimeToleranceMinutes) {
    public UpdateFlightPolicyRequest {
        if (flightRetentionDays == null || actualTimeToleranceMinutes == null) {
            throw new IllegalArgumentException("All flight policy settings are required");
        }
    }

    public UpdateFlightPolicyCommand toCommand() {
        return new UpdateFlightPolicyCommand(new FlightPolicy(flightRetentionDays, actualTimeToleranceMinutes));
    }
}
