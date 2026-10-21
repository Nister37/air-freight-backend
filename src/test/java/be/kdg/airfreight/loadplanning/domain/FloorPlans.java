package be.kdg.airfreight.loadplanning.domain;

import java.util.List;
import java.util.stream.Stream;

final class FloorPlans {
    private FloorPlans() { }

    static final FloorPlan B747_400F = new MainDeckFloorPlan(
            Stream.of("C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "P", "R", "S", "T", "U").map(Slot::new).toList(),
            List.of(new Slot("A1"), new Slot("A2"), new Slot("B1")),
            List.of(new Slot("X")),
            new Slot("R"));

    static Position pos(String code) {
        return B747_400F.find(code).orElseThrow(() -> new IllegalArgumentException("No position " + code));
    }
}
