package be.kdg.airfreight.flightoperations.domain.vo;

import java.util.Objects;
import java.util.UUID;

public record DelayId(UUID value) {
    public DelayId { Objects.requireNonNull(value); }
    public static DelayId create() { return new DelayId(UUID.randomUUID()); }
}
