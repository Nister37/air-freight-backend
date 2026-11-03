package be.kdg.airfreight.loadplanning.domain.vo;

import java.util.List;
import java.util.Optional;

/** The available positions and unloading routes of an aircraft layout. */
public interface FloorPlan {
    List<Position> positions();
    boolean contains(Position position);
    Optional<Position> find(String code);

    /**
     * Every way a ULD on `from` can reach the door. Each route lists the positions that must be clear.
     * The ULD can be unloaded if at least one route is clear; its own position is excluded from the route.
     */
    List<List<Position>> routesToDoor(Position from);
}
