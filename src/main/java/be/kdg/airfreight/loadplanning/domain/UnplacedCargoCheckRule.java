package be.kdg.airfreight.loadplanning.domain;

import java.util.List;

final class UnplacedCargoCheckRule implements LoadCheckRule {
    @Override
    public LoadCheck check(LoadView load, FloorPlan floorPlan, LoadRules rules) {
        List<String> unplaced = load.ulds().stream()
                .filter(uld -> load.placements().stream().noneMatch(placement -> placement.code().equals(uld.code())))
                .map(uld -> uld.code().value()).toList();
        return unplaced.isEmpty()
                ? LoadCheck.passed(CheckKind.NOT_ALL_CARGO_LOADED, Severity.SOFT, "All booked ULDs have a position")
                : LoadCheck.failed(CheckKind.NOT_ALL_CARGO_LOADED, Severity.SOFT,
                "No position, will stay behind: " + String.join(", ", unplaced));
    }
}
