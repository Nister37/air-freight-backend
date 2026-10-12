package be.kdg.airfreight.loadplanning.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record LoadRules(Severity offloadingSeverity, BigDecimal maxLateralDifferencePercent) {
    public LoadRules {
        Objects.requireNonNull(offloadingSeverity);
        Objects.requireNonNull(maxLateralDifferencePercent);
        if (maxLateralDifferencePercent.signum() < 0) {
            throw new IllegalArgumentException("Max lateral difference cannot be negative");
        }
    }
}