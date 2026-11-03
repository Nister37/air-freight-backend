package be.kdg.airfreight.flightoperations.domain.entity;

import be.kdg.airfreight.flightoperations.domain.vo.DelayCode;
import be.kdg.airfreight.flightoperations.domain.vo.DelayId;

import java.util.Objects;

public record Delay(DelayId id, DelayCode code, String description, int minutes) {
    public Delay {
        Objects.requireNonNull(id);
        Objects.requireNonNull(code);
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Delay description is required");
        if (minutes < 1 || minutes > 720) throw new IllegalArgumentException("Delay must be between one minute and twelve hours");
    }

    public Delay corrected(int newMinutes, String newDescription) {
        return new Delay(id, code, newDescription, newMinutes);
    }
}
