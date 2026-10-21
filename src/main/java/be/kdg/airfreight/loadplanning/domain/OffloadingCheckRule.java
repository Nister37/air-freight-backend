package be.kdg.airfreight.loadplanning.domain;

import be.kdg.airfreight.common.Airport;

import java.util.List;
import java.util.stream.Collectors;

final class OffloadingCheckRule implements LoadCheckRule {
    @Override
    public LoadCheck check(LoadView load, FloorPlan floorPlan, LoadRules rules) {
        List<String> stuck = blockages(load, floorPlan).stream()
                .map(blockage -> blockage.uldCode().value() + " on " + blockage.position().code() + " blocked by "
                        + blockage.blockers().stream().map(blocker -> blocker.code().value() + " on " + blocker.position().code())
                        .collect(Collectors.joining(", ")))
                .toList();
        return stuck.isEmpty()
                ? LoadCheck.passed(CheckKind.OFFLOADING, rules.offloadingSeverity(),
                "All ULDs for " + load.destinationAirport().value() + " can be unloaded")
                : LoadCheck.failed(CheckKind.OFFLOADING, rules.offloadingSeverity(),
                "Blocked by ULDs for another destination: " + String.join(", ", stuck));
    }

    List<OffloadingBlockage> blockages(LoadView load, FloorPlan floorPlan) {
        return load.placements().stream()
                .filter(placement -> destinationOf(load, placement.code()).equals(load.destinationAirport()))
                .filter(placement -> !canBeUnloaded(load, placement.position(), floorPlan))
                .map(placement -> {
                    List<Position> blockedPositions = floorPlan.routesToDoor(placement.position()).stream()
                            .flatMap(List::stream).filter(position -> holdsOtherDestination(load, position)).distinct().toList();
                    List<ULDPlacement> blockers = load.placements().stream()
                            .filter(candidate -> blockedPositions.contains(candidate.position())).toList();
                    return new OffloadingBlockage(placement.code(), placement.position(), blockers);
                }).toList();
    }

    private boolean canBeUnloaded(LoadView load, Position from, FloorPlan floorPlan) {
        return floorPlan.routesToDoor(from).stream()
                .anyMatch(route -> route.stream().noneMatch(position -> holdsOtherDestination(load, position)));
    }

    private boolean holdsOtherDestination(LoadView load, Position position) {
        return load.placements().stream().filter(placement -> placement.position().equals(position))
                .anyMatch(placement -> !destinationOf(load, placement.code()).equals(load.destinationAirport()));
    }

    private Airport destinationOf(LoadView load, ULDCode code) {
        return load.ulds().stream().filter(uld -> uld.code().equals(code)).findFirst().orElseThrow().destination();
    }
}
