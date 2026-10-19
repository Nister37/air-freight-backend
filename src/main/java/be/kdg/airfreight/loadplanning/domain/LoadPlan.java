package be.kdg.airfreight.loadplanning.domain;

import java.util.Objects;

public record LoadPlan(LoadView load, FloorPlan floorPlan) {
    public LoadPlan {
        Objects.requireNonNull(load);
        Objects.requireNonNull(floorPlan);
    }
}
