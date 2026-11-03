package be.kdg.airfreight.loadplanning.domain.vo;

import be.kdg.airfreight.loadplanning.domain.enums.WeightUnit;

import java.math.BigDecimal;
import java.util.Objects;

public record Weight(BigDecimal amount, WeightUnit unit) {
    private static final int MAX_INTEGER_DIGITS = 13;
    private static final int MAX_FRACTIONAL_DIGITS = 6;

    public Weight {
        Objects.requireNonNull(amount, "weight amount is required");
        Objects.requireNonNull(unit, "weight unit is required");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }
        amount = amount.stripTrailingZeros();
        if (amount.scale() > MAX_FRACTIONAL_DIGITS
                || (long) amount.precision() - amount.scale() > MAX_INTEGER_DIGITS) {
            throw new IllegalArgumentException("Weight must have at most 13 integer and 6 fractional digits");
        }
        if (amount.scale() < 0) {
            amount = amount.setScale(0);
        }
    }

    public BigDecimal toKg() { return unit.toKg(amount); }
}
