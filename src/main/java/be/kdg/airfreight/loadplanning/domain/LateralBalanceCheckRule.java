package be.kdg.airfreight.loadplanning.domain;

import be.kdg.airfreight.loadplanning.domain.enums.CheckKind;
import be.kdg.airfreight.loadplanning.domain.enums.Severity;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class LateralBalanceCheckRule implements LoadCheckRule {
    @Override
    public LoadCheck check(LoadView load, FloorPlan floorPlan, LoadRules rules) {
        BigDecimal left = BigDecimal.ZERO;
        BigDecimal right = BigDecimal.ZERO;
        for (ULDPlacement placement : load.placements()) {
            Weight weight = load.ulds().stream()
                    .filter(uld -> uld.code().equals(placement.code()))
                    .findFirst().orElseThrow().weight();
            left = left.add(placement.position().leftWeightInKg(weight));
            right = right.add(placement.position().rightWeightInKg(weight));
        }
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

    // Round report text only; comparisons use the exact weights.
    private static String display(BigDecimal kg) {
        return kg.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
