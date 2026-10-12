package be.kdg.airfreight.loadplanning.domain;

import java.util.Objects;

public record Slot(String value) {

    public Slot {
        Objects.requireNonNull(value, "slot is required");
        if (value.isBlank()) {
            throw new IllegalArgumentException("slot value cannot be blank");
        }
    }
}
