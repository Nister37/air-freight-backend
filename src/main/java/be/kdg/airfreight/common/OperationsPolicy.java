package be.kdg.airfreight.common;

import java.math.BigDecimal;
import java.util.Objects;

/** Persisted operational settings, shared as values rather than a policy entity. */
public record OperationsPolicy(Severity offloadingConstraint, int loadingWindowOpeningMinutes,
                               int loadingWindowClosingMinutes, BigDecimal maximumLateralWeightDifferencePercent,
                               int flightRetentionDays, int actualTimeToleranceMinutes) {
    public enum Severity { HARD, SOFT }

    public OperationsPolicy {
        Objects.requireNonNull(offloadingConstraint);
        Objects.requireNonNull(maximumLateralWeightDifferencePercent);
        maximumLateralWeightDifferencePercent = maximumLateralWeightDifferencePercent.stripTrailingZeros();
        if (maximumLateralWeightDifferencePercent.scale() > 6
                || (long) maximumLateralWeightDifferencePercent.precision() - maximumLateralWeightDifferencePercent.scale() > 13) {
            throw new IllegalArgumentException("Weight difference must fit the persisted percentage precision (13 integer and 6 fractional digits)");
        }
        if (maximumLateralWeightDifferencePercent.scale() < 0) {
            maximumLateralWeightDifferencePercent = maximumLateralWeightDifferencePercent.setScale(0);
        }
        if (loadingWindowClosingMinutes < 0
                || loadingWindowOpeningMinutes <= loadingWindowClosingMinutes
                || maximumLateralWeightDifferencePercent.signum() < 0
                || flightRetentionDays < 1 || actualTimeToleranceMinutes < 0) {
            throw new IllegalArgumentException("Invalid policy: require a positive loading window and retention, and non-negative tolerance and weight difference");
        }
    }

    public static OperationsPolicy defaults() {
        return new OperationsPolicy(Severity.HARD, 720, 20, new BigDecimal("10"), 7, 5);
    }

}
