package be.kdg.airfreight.loadplanning.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record FloorPlan(Position doorPosition, List<Position> positions) {

    public FloorPlan{
        Objects.requireNonNull(doorPosition, "Door position is required");
        Objects.requireNonNull(positions, "Positions are required");
        positions = List.copyOf(positions);

        if (positions.isEmpty()) {
            throw new IllegalArgumentException("Floor Plan needs at least one position");
        }
        if (new HashSet<>(positions).size() != positions.size()) {
            throw new IllegalArgumentException("Floor Plan has duplicate positions");
        }
        if (!positions.contains(doorPosition)) {
            throw new IllegalArgumentException("Door position must be one of the positions");
        }

    }
    public boolean contains(Position position){
        return positions.contains(position);
    }

    public Optional<Position> find(String code){
        return positions.stream().filter(p -> p.code().equals(code)).findFirst();
    }

}
