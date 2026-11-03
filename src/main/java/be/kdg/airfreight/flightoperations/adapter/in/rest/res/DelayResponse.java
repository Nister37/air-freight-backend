package be.kdg.airfreight.flightoperations.adapter.in.rest.res;

import be.kdg.airfreight.flightoperations.domain.enums.DelayPhase;
import be.kdg.airfreight.flightoperations.domain.entity.Delay;
import java.util.UUID;

public record DelayResponse(UUID id, String code, String description, int minutes, DelayPhase phase) {
    public static DelayResponse from(Delay delay, DelayPhase phase) {
        return new DelayResponse(delay.id().value(), delay.code().value(), delay.description(), delay.minutes(), phase);
    }
}
