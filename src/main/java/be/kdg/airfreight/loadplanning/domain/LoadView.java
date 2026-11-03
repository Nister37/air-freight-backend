package be.kdg.airfreight.loadplanning.domain;

import be.kdg.airfreight.loadplanning.domain.entity.ULD;
import be.kdg.airfreight.loadplanning.domain.vo.FloorPlan;
import be.kdg.airfreight.loadplanning.domain.vo.LoadCheck;
import be.kdg.airfreight.loadplanning.domain.vo.LoadingWindow;
import be.kdg.airfreight.loadplanning.domain.vo.Position;
import be.kdg.airfreight.loadplanning.domain.vo.ULDCode;
import be.kdg.airfreight.loadplanning.domain.vo.ULDPlacement;

import be.kdg.airfreight.loadplanning.domain.enums.LoadStatus;
import be.kdg.airfreight.loadplanning.domain.enums.Result;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Immutable load data for queries; it exposes no aggregate mutations. */
public record LoadView(FlightId flightId, AircraftRegistration aircraftRegistration, Airport destinationAirport,
                       LoadingWindow loadingWindow, LoadStatus status, List<ULD> ulds,
                       List<ULDPlacement> placements, List<LoadCheck> checks) {
    public LoadView {
        Objects.requireNonNull(flightId);
        Objects.requireNonNull(aircraftRegistration);
        Objects.requireNonNull(destinationAirport);
        Objects.requireNonNull(loadingWindow);
        Objects.requireNonNull(status);
        ulds = List.copyOf(ulds);
        placements = List.copyOf(placements);
        checks = List.copyOf(checks);
    }

    public LoadFacts facts() {
        List<UldDetails> uldDetails = ulds.stream()
                .map(uld -> new UldDetails(uld.code().value(), uld.weight().amount(), uld.weight().unit().name(),
                        uld.destination(), positionOf(uld.code()).map(Position::code).orElse(null))).toList();
        List<LoadCheckDetails> checkDetails = checks.stream()
                .map(check -> new LoadCheckDetails(check.kind().name(), check.severity().name(),
                        check.result() == Result.PASSED, check.explanation())).toList();
        return new LoadFacts(flightId, aircraftRegistration, destinationAirport, loadingWindow.opensAt(),
                loadingWindow.closesAt(), status == LoadStatus.FINAL, uldDetails, checkDetails);
    }

    public List<OffloadingBlockage> blockages(FloorPlan floorPlan) {
        return new OffloadingCheckRule().blockages(this, floorPlan);
    }

    private Optional<Position> positionOf(ULDCode code) {
        return placements.stream().filter(placement -> placement.code().equals(code))
                .map(ULDPlacement::position).findFirst();
    }
}
