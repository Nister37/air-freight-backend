package be.kdg.airfreight.loadplanning.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class LateralBalanceCheckRule implements LoadCheckRule {
    @Override
    public LoadCheck check(LoadView load, FloorPlan floorPlan, LoadRules rules) {
        BigDecimal left = weightOnSide(load, Side.LEFT);
        BigDecimal right = weightOnSide(load, Side.RIGHT);
        BigDecimal heavier = left.max(right);
        BigDecimal lighter = left.min(right);
        BigDecimal factor = BigDecimal.ONE.add(rules.maxLateralDifferencePercent().movePointLeft(2));
        BigDecimal maxHeavier = lighter.multiply(factor);
        String detail = "Left " + display(left) + " kg, right " + display(right) + " kg; heavier side may carry at most "
                + display(maxHeavier) + " kg (limit " + rules.maxLateralDifferencePercent().toPlainString() + "%)";
        return heavier.compareTo(maxHeavier) <= 0
                ? LoadCheck.passed(CheckKind.LATERAL_BALANCE, Severity.HARD, detail)
                : LoadCheck.failed(CheckKind.LATERAL_BALANCE, Severity.HARD, detail);
    }

    private BigDecimal weightOnSide(LoadView load, Side side) {
        return load.placements().stream().filter(placement -> placement.position().side() == side)
                .map(placement -> load.ulds().stream().filter(uld -> uld.code().equals(placement.code()))
                        .findFirst().orElseThrow().weight().toKg())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Round report text only; comparisons use the exact weights.
    private static String display(BigDecimal kg) {
        return kg.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
