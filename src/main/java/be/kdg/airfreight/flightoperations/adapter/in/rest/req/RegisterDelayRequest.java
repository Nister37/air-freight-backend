package be.kdg.airfreight.flightoperations.adapter.in.rest.req;

import be.kdg.airfreight.flightoperations.domain.enums.DelayPhase;

public record RegisterDelayRequest(DelayPhase phase, String code, int minutes, String description) {
    public RegisterDelayRequest {
        if (phase == null) throw new IllegalArgumentException("Delay phase is required");
        if (code == null) throw new IllegalArgumentException("Delay code is required");
        if (description == null) throw new IllegalArgumentException("Delay description is required");
    }
}
