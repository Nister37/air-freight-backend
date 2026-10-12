package be.kdg.airfreight.loadplanning.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record Weight(BigDecimal amount, WeightUnit unit) {
    public Weight {
        Objects.requireNonNull(amount, "weight amount is required");
        Objects.requireNonNull(unit, "weight unit is required");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }
    }

    public BigDecimal toKg() { return unit.toKg(amount); }
}
