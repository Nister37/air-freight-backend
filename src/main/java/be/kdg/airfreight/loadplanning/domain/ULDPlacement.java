package be.kdg.airfreight.loadplanning.domain;

import java.util.Objects;

public record ULDPlacement(ULDCode code, Position position) {
    public ULDPlacement {
        Objects.requireNonNull(code, "ULD code is required");
        Objects.requireNonNull(position, "position is required");
    }
}
