package be.kdg.airfreight.loadplanning.domain.vo;

import be.kdg.airfreight.loadplanning.domain.enums.Severity;

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
