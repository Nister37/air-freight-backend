package be.kdg.airfreight.loadplanning.domain;

import java.util.Objects;

public record Position(Side side, Slot slot) {

    public Position{
        Objects.requireNonNull(side, "side is required");
        Objects.requireNonNull(slot, "slot is required");
    }

    public String code(){
        return side.prefix() + slot.value();
    }
}
