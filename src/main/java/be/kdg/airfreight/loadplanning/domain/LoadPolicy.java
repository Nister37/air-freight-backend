package be.kdg.airfreight.loadplanning.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record LoadPolicy(Severity offloadingConstraint, int loadingWindowOpeningMinutes,
                         int loadingWindowClosingMinutes, BigDecimal maximumLateralWeightDifferencePercent) {
    private static final Severity DEFAULT_OFFLOADING_CONSTRAINT = Severity.HARD;
    private static final int DEFAULT_LOADING_WINDOW_OPENING_MINUTES = 720;
    private static final int DEFAULT_LOADING_WINDOW_CLOSING_MINUTES = 20;
    private static final BigDecimal DEFAULT_MAXIMUM_LATERAL_WEIGHT_DIFFERENCE_PERCENT = new BigDecimal("10");

    public LoadPolicy {
        Objects.requireNonNull(offloadingConstraint, "Offloading constraint is required");
        Objects.requireNonNull(maximumLateralWeightDifferencePercent, "Weight difference is required");
        maximumLateralWeightDifferencePercent = maximumLateralWeightDifferencePercent.stripTrailingZeros();
        if (maximumLateralWeightDifferencePercent.scale() > 6
                || (long) maximumLateralWeightDifferencePercent.precision()
                - maximumLateralWeightDifferencePercent.scale() > 13) {
            throw new IllegalArgumentException("Weight difference must fit the persisted percentage precision (13 integer and 6 fractional digits)");
        }
        if (maximumLateralWeightDifferencePercent.scale() < 0) {
            maximumLateralWeightDifferencePercent = maximumLateralWeightDifferencePercent.setScale(0);
        }
        if (loadingWindowClosingMinutes < 0 || loadingWindowOpeningMinutes <= loadingWindowClosingMinutes
                || maximumLateralWeightDifferencePercent.signum() < 0) {
            throw new IllegalArgumentException("Load policy requires a positive loading window and non-negative weight difference");
        }
    }

    public static LoadPolicy defaults() {
        return new LoadPolicy(DEFAULT_OFFLOADING_CONSTRAINT, DEFAULT_LOADING_WINDOW_OPENING_MINUTES,
                DEFAULT_LOADING_WINDOW_CLOSING_MINUTES, DEFAULT_MAXIMUM_LATERAL_WEIGHT_DIFFERENCE_PERCENT);
    }
}
