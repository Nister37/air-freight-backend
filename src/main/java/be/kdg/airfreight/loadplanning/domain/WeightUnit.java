package be.kdg.airfreight.loadplanning.domain;

import java.math.BigDecimal;

public enum WeightUnit {
    KG(BigDecimal.ONE),
    LB(new BigDecimal("0.45359237"));

    private final BigDecimal kgFactor;

    WeightUnit(BigDecimal kgFactor) {
        this.kgFactor = kgFactor;
    }

    BigDecimal toKg(BigDecimal amount) {
        return amount.multiply(kgFactor);
    }

}
