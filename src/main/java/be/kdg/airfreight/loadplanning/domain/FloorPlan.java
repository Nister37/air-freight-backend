package be.kdg.airfreight.loadplanning.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Main deck layout, nose to tail: nose slots, rows with a LEFT and RIGHT position, tail slots.
 * The cargo door is on the LEFT side of doorRow.
 */
public record FloorPlan(List<Slot> rows, List<Slot> noseSlots, List<Slot> tailSlots, Slot doorRow) {

    public FloorPlan {
        Objects.requireNonNull(rows, "rows are required");
        Objects.requireNonNull(noseSlots, "nose slots are required");
        Objects.requireNonNull(tailSlots, "tail slots are required");
        Objects.requireNonNull(doorRow, "door row is required");
        rows = List.copyOf(rows);
        noseSlots = List.copyOf(noseSlots);
        tailSlots = List.copyOf(tailSlots);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Floor plan needs at least one row");
        }
        if (!rows.contains(doorRow)) {
            throw new IllegalArgumentException("Door row must be one of the rows");
        }
        List<String> codes = allPositions(rows, noseSlots, tailSlots).stream().map(Position::code).toList();
        if (new HashSet<>(codes).size() != codes.size()) {
            throw new IllegalArgumentException("Floor plan has duplicate position codes");
        }
    }

    // ---------- positions ----------

    public List<Position> positions() {
        return allPositions(rows, noseSlots, tailSlots);
    }

    public Position doorPosition() {
        return new Position(Side.LEFT, doorRow);
    }

    public boolean contains(Position position) {
        return positions().contains(position);
    }

    public Optional<Position> find(String code) {
        return positions().stream().filter(p -> p.code().equals(code)).findFirst();
    }

    // ---------- routes to the door ----------

    /**
     * Every way a ULD on `from` can reach the door. Each route lists the positions that must be clear.
     * The ULD can be unloaded if at least ONE route is clear. Rows give one route; nose/tail give two (left or right).
     */
    public List<List<Position>> routesToDoor(Position from) {
        if (!contains(from)) {
            throw new IllegalArgumentException("Position " + from.code() + " is not on this floor plan");
        }
        return switch (from.side()) {
            case LEFT, RIGHT -> {
                List<Position> path = pathAlongSide(from.side(), rows.indexOf(from.slot()));
                path.remove(0);   // the ULD's own position doesn't block itself
                yield List.of(List.copyOf(path));
            }
            case NOSE -> manoeuvre(0);
            case TAIL -> manoeuvre(rows.size() - 1);
        };
    }

    /** Nose/tail: both positions of the nearest row must be clear, then go left OR right along that side. */
    private List<List<Position>> manoeuvre(int nearestRow) {
        Slot row = rows.get(nearestRow);
        List<Position> mustBeClear = List.of(new Position(Side.LEFT, row), new Position(Side.RIGHT, row));
        return List.of(
                union(mustBeClear, pathAlongSide(Side.LEFT, nearestRow)),
                union(mustBeClear, pathAlongSide(Side.RIGHT, nearestRow))
        );
    }

    /** From a row (inclusive) along one side to the door row (inclusive). On the RIGHT, cross over to the door. */
    private List<Position> pathAlongSide(Side side, int fromRow) {
        int doorIndex = rows.indexOf(doorRow);
        int step = fromRow <= doorIndex ? 1 : -1;
        List<Position> path = new ArrayList<>();
        for (int i = fromRow; ; i += step) {
            path.add(new Position(side, rows.get(i)));
            if (i == doorIndex) break;
        }
        if (side == Side.RIGHT) {
            path.add(doorPosition());
        }
        return path;
    }

    private static List<Position> union(List<Position> a, List<Position> b) {
        Set<Position> result = new LinkedHashSet<>(a);
        result.addAll(b);
        return List.copyOf(result);
    }

    private static List<Position> allPositions(List<Slot> rows, List<Slot> nose, List<Slot> tail) {
        List<Position> all = new ArrayList<>();
        nose.forEach(s -> all.add(new Position(Side.NOSE, s)));
        rows.forEach(s -> {
            all.add(new Position(Side.LEFT, s));
            all.add(new Position(Side.RIGHT, s));
        });
        tail.forEach(s -> all.add(new Position(Side.TAIL, s)));
        return all;
    }
}
