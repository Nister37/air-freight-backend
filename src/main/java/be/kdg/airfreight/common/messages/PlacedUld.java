package be.kdg.airfreight.common.messages;

import be.kdg.airfreight.common.Airport;

import java.math.BigDecimal;
import java.util.Objects;

/** Final-load contract supplied by Load Planning; contains placed cargo only. */
public record PlacedUld(String uldCode, String position, BigDecimal weight, String unit, Airport destination) {
    public PlacedUld {
        if (uldCode == null || !uldCode.matches("[A-Z]{3}[0-9]{4,5}[A-Z0-9]{2}")) {
            throw new IllegalArgumentException("Invalid ULD code");
        }
        if (position == null || position.isBlank()) throw new IllegalArgumentException("ULD position is required");
        if (weight == null || weight.signum() <= 0) throw new IllegalArgumentException("ULD weight must be positive");
        if (!"KG".equals(unit) && !"LB".equals(unit)) throw new IllegalArgumentException("Weight unit must be KG or LB");
        Objects.requireNonNull(destination);
    }
}
