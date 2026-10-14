package be.kdg.airfreight.loadplanning.domain;

import java.util.Objects;

public record Position(Side side, Slot slot) {
    public Position {
        Objects.requireNonNull(side, "side is required");
        Objects.requireNonNull(slot, "slot is required");
    }

    /** "CL", "CR", ... for rows; "A1", "B1", "X" for nose/tail. Used in PlacedUld and the CPM. */
    public String code() {
        return switch (side) {
            case LEFT -> slot.value() + "L";
            case RIGHT -> slot.value() + "R";
            case NOSE, TAIL -> slot.value();
        };
    }
}

