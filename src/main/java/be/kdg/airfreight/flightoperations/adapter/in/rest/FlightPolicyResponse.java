package be.kdg.airfreight.flightoperations.adapter.in.rest;

import be.kdg.airfreight.flightoperations.domain.FlightPolicy;

public record FlightPolicyResponse(int flightRetentionDays, int actualTimeToleranceMinutes) {
    public static FlightPolicyResponse from(FlightPolicy policy) {
        return new FlightPolicyResponse(policy.flightRetentionDays(), policy.actualTimeToleranceMinutes());
    }
}
