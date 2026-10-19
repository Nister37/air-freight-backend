package be.kdg.airfreight.loadplanning.domain;

import be.kdg.airfreight.common.Airport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Calculates checks without changing the load. */
final class LoadChecks {
    private final List<ULD> ulds;
    private final List<ULDPlacement> placements;
    private final Airport destinationAirport;

    LoadChecks(List<ULD> ulds, List<ULDPlacement> placements, Airport destinationAirport) {
        this.ulds = List.copyOf(ulds);
        this.placements = List.copyOf(placements);
        this.destinationAirport = destinationAirport;
    }

    List<LoadCheck> check(FloorPlan floorPlan, LoadRules rules) {
        return List.of(lateralBalanceCheck(rules), offloadingCheck(floorPlan, rules), notAllCargoLoadedCheck());
    }

    private LoadCheck lateralBalanceCheck(LoadRules rules) {
        BigDecimal left = weightOnSide(Side.LEFT);
        BigDecimal right = weightOnSide(Side.RIGHT);
        BigDecimal heavier = left.max(right);
        BigDecimal lighter = left.min(right);
        BigDecimal factor = BigDecimal.ONE.add(rules.maxLateralDifferencePercent().movePointLeft(2));
        BigDecimal maxHeavier = lighter.multiply(factor);
        String detail = "Left " + display(left) + " kg, right " + display(right) + " kg; heavier side may carry at most "
                + display(maxHeavier) + " kg (limit " + rules.maxLateralDifferencePercent().toPlainString() + "%)";
        return heavier.compareTo(maxHeavier) <= 0
                ? LoadCheck.passed(CheckKind.LATERAL_BALANCE, Severity.HARD, detail)
                : LoadCheck.failed(CheckKind.LATERAL_BALANCE, Severity.HARD, detail);
    }

    private LoadCheck offloadingCheck(FloorPlan floorPlan, LoadRules rules) {
        List<String> stuck = blockages(floorPlan).stream()
                .map(blockage -> blockage.uldCode().value() + " on " + blockage.position().code() + " blocked by "
                        + blockage.blockers().stream().map(blocker -> blocker.code().value() + " on " + blocker.position().code())
                        .collect(java.util.stream.Collectors.joining(", ")))
                .toList();
        return stuck.isEmpty()
                ? LoadCheck.passed(CheckKind.OFFLOADING, rules.offloadingSeverity(),
                "All ULDs for " + destinationAirport.value() + " can be unloaded")
                : LoadCheck.failed(CheckKind.OFFLOADING, rules.offloadingSeverity(),
                "Blocked by ULDs for another destination: " + String.join(", ", stuck));
    }

    List<OffloadingBlockage> blockages(FloorPlan floorPlan) {
        return placements.stream()
                .filter(placement -> destinationOf(placement.code()).equals(destinationAirport))
                .filter(placement -> !canBeUnloaded(placement.position(), floorPlan))
                .map(placement -> {
                    List<Position> blockedPositions = floorPlan.routesToDoor(placement.position()).stream()
                            .flatMap(List::stream).filter(this::holdsOtherDestination).distinct().toList();
                    List<ULDPlacement> blockers = placements.stream()
                            .filter(candidate -> blockedPositions.contains(candidate.position())).toList();
                    return new OffloadingBlockage(placement.code(), placement.position(), blockers);
                }).toList();
    }

    private LoadCheck notAllCargoLoadedCheck() {
        List<String> unplaced = ulds.stream()
                .filter(uld -> placements.stream().noneMatch(placement -> placement.code().equals(uld.code())))
                .map(uld -> uld.code().value()).toList();
        return unplaced.isEmpty()
                ? LoadCheck.passed(CheckKind.NOT_ALL_CARGO_LOADED, Severity.SOFT, "All booked ULDs have a position")
                : LoadCheck.failed(CheckKind.NOT_ALL_CARGO_LOADED, Severity.SOFT,
                "No position, will stay behind: " + String.join(", ", unplaced));
    }

    private boolean canBeUnloaded(Position from, FloorPlan floorPlan) {
        return floorPlan.routesToDoor(from).stream()
                .anyMatch(route -> route.stream().noneMatch(this::holdsOtherDestination));
    }

    private boolean holdsOtherDestination(Position position) {
        return placements.stream().filter(placement -> placement.position().equals(position))
                .anyMatch(placement -> !destinationOf(placement.code()).equals(destinationAirport));
    }

    private Airport destinationOf(ULDCode code) {
        return uld(code).destination();
    }

    private BigDecimal weightOnSide(Side side) {
        return placements.stream().filter(placement -> placement.position().side() == side)
                .map(placement -> uld(placement.code()).weight().toKg()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ULD uld(ULDCode code) {
        return ulds.stream().filter(uld -> uld.code().equals(code)).findFirst().orElseThrow();
    }

    // Round report text only; comparisons use the exact weights.
    private static String display(BigDecimal kg) {
        return kg.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
