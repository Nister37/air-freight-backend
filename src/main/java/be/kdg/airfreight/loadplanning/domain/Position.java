package be.kdg.airfreight.loadplanning.domain;

import java.math.BigDecimal;

/** A floor-plan position identifies itself and defines its contribution to lateral balance. */
public interface Position {
    String code();
    BigDecimal leftWeightInKg(Weight weight);
    BigDecimal rightWeightInKg(Weight weight);
}

