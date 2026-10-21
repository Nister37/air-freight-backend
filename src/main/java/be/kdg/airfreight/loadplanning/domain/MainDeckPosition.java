package be.kdg.airfreight.loadplanning.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record MainDeckPosition(Side side, Slot slot) implements Position {
    public MainDeckPosition {
        Objects.requireNonNull(side, "side is required");
        Objects.requireNonNull(slot, "slot is required");
    }

    /** "CL", "CR", ... for rows; "A1", "B1", "X" for nose/tail. Used in PlacedUld and the CPM. */
    @Override
    public String code() {
        return switch (side) {
            case LEFT -> slot.value() + "L";
            case RIGHT -> slot.value() + "R";
            case NOSE, TAIL -> slot.value();
        };
    }

    @Override
    public BigDecimal leftWeightInKg(Weight weight) {
        Objects.requireNonNull(weight);
        return side == Side.LEFT ? weight.toKg() : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal rightWeightInKg(Weight weight) {
        Objects.requireNonNull(weight);
        return side == Side.RIGHT ? weight.toKg() : BigDecimal.ZERO;
    }
}
