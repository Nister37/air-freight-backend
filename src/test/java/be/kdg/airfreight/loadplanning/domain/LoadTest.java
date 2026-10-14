package be.kdg.airfreight.loadplanning.domain;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.events.LoadFinalisedEvent;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class LoadTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 11);
    private static final LocalDateTime STD = LocalDateTime.parse("2026-10-11T15:00:00");
    private static final LocalDateTime NOW = LocalDateTime.parse("2026-10-11T10:00:00");
    private static final LocalDateTime BEFORE_WINDOW = LocalDateTime.parse("2026-10-11T02:00:00");
    private static final LocalDateTime AFTER_WINDOW = LocalDateTime.parse("2026-10-11T14:40:00");

    private static final FlightId FLIGHT_ID = new FlightId(new FlightNumber("HJ221"), DATE, new Airport("BRU"));
    private static final AircraftRegistration REG = new AircraftRegistration("SXBAA");
    private static final Airport ATH = new Airport("ATH");
    private static final Airport CDG = new Airport("CDG");

    private static final Position L1 = FloorPlans.pos("CL");
    private static final Position R1 = FloorPlans.pos("CR");
    private static final Position N1 = FloorPlans.pos("A1");
    private static final FloorPlan FLOOR_PLAN = FloorPlans.B747_400F;

    private static final ULDCode CODE_A = new ULDCode("AKE11111AA");
    private static final ULDCode CODE_B = new ULDCode("AKE22222BB");
    private static final ULDCode CODE_C = new ULDCode("AKE33333CC");

    private static final LoadRules SOFT_RULES = new LoadRules(Severity.SOFT, new BigDecimal("10"));
    private static final LoadRules HARD_RULES = new LoadRules(Severity.HARD, new BigDecimal("10"));

    private static Load openLoad() {
        return Load.open(LoadId.create(), FLIGHT_ID, REG, ATH, LoadingWindow.beforeDeparture(STD, 720, 20));
    }

    private static ULD uld(ULDCode code, String kg, Airport destination) {
        return ULD.of(code, new Weight(new BigDecimal(kg), WeightUnit.KG), destination);
    }

    private static Load loadWithOneUnplacedUld() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.book(uld(CODE_B, "500", ATH), NOW);
        load.place(CODE_A, N1, FLOOR_PLAN, NOW);
        return load;
    }

    // open

    @Test
    void newLoadIsOpenWithNoUldsAndNoPlacements() {
        Load load = openLoad();
        assertThat(load.status()).isEqualTo(LoadStatus.OPEN);
        assertThat(load.ulds()).isEmpty();
        assertThat(load.placements()).isEmpty();
    }

    // book

    @Test
    void bookingAddsUldWithStatusBooked() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        assertThat(load.ulds()).hasSize(1);
        assertThat(load.statusOf(CODE_A)).isEqualTo(ULDStatus.BOOKED);
    }

    @Test
    void firstBookingReturnsTrue() {
        Load load = openLoad();
        assertThat(load.book(uld(CODE_A, "1000", ATH), NOW)).isTrue();
    }

    @Test
    void duplicateBookingIsIgnoredEvenWithDifferentData() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);

        boolean booked = load.book(uld(CODE_A, "2000", CDG), NOW);

        assertThat(booked).isFalse();
        assertThat(load.ulds()).singleElement().satisfies(u -> {
            assertThat(u.weight().amount()).isEqualByComparingTo("1000");
            assertThat(u.destination()).isEqualTo(ATH);
        });
    }

    @Test
    void duplicateBookingAfterFinaliseIsIgnoredInsteadOfFailing() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.place(CODE_A, N1, FLOOR_PLAN, NOW);
        load.finalise(false, FLOOR_PLAN, SOFT_RULES, NOW);

        assertThat(load.book(uld(CODE_A, "1000", ATH), NOW)).isFalse();
    }

    @Test
    void bookingAfterWindowClosingThrows() {
        Load load = openLoad();
        assertThatThrownBy(() -> load.book(uld(CODE_A, "1000", ATH), AFTER_WINDOW))
                .isInstanceOf(IllegalStateException.class);
    }

    // place

    @Test
    void placingBookedUldChangesStatusToPlacedAndAppearsInPlacements() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.place(CODE_A, L1, FLOOR_PLAN, NOW);
        assertThat(load.statusOf(CODE_A)).isEqualTo(ULDStatus.PLACED);
        assertThat(load.placements()).containsExactly(new ULDPlacement(CODE_A, L1));
    }

    @Test
    void placingUnbookedUldThrows() {
        Load load = openLoad();
        assertThatThrownBy(() -> load.place(CODE_A, L1, FLOOR_PLAN, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void placingOnPositionAbsentFromFloorPlanThrows() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        Position unknown = new Position(Side.TAIL, new Slot("Z"));
        assertThatThrownBy(() -> load.place(CODE_A, unknown, FLOOR_PLAN, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void placingSameUldTwiceThrows() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.place(CODE_A, L1, FLOOR_PLAN, NOW);
        assertThatThrownBy(() -> load.place(CODE_A, R1, FLOOR_PLAN, NOW))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void placingTwoUldsOnSamePositionThrows() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.book(uld(CODE_B, "500", ATH), NOW);
        load.place(CODE_A, L1, FLOOR_PLAN, NOW);
        assertThatThrownBy(() -> load.place(CODE_B, L1, FLOOR_PLAN, NOW))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void placingBeforeWindowOpensThrows() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), BEFORE_WINDOW);
        assertThatThrownBy(() -> load.place(CODE_A, L1, FLOOR_PLAN, BEFORE_WINDOW))
                .isInstanceOf(IllegalStateException.class);
    }

    // removePlacement

    @Test
    void removingPlacementRestoresUldToBookedAndKeepsItInUlds() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.place(CODE_A, L1, FLOOR_PLAN, NOW);
        load.removePlacement(CODE_A, NOW);
        assertThat(load.statusOf(CODE_A)).isEqualTo(ULDStatus.BOOKED);
        assertThat(load.ulds()).hasSize(1);
        assertThat(load.placements()).isEmpty();
    }

    @Test
    void removingUnplacedUldThrows() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        assertThatThrownBy(() -> load.removePlacement(CODE_A, NOW))
                .isInstanceOf(IllegalStateException.class);
    }

    // check

    @Test
    void uldWithAnotherDestinationIsAllowedOnBoard() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", CDG), NOW);
        load.place(CODE_A, N1, FLOOR_PLAN, NOW);
        List<LoadCheck> checks = load.check(FLOOR_PLAN, SOFT_RULES);
        assertThat(checks).noneMatch(LoadCheck::isBlocking);
    }

    @Test
    void checkRunsAllThreeChecksInSpecOrder() {
        Load load = openLoad();
        assertThat(load.check(FLOOR_PLAN, SOFT_RULES))
                .extracting(LoadCheck::kind)
                .containsExactly(CheckKind.LATERAL_BALANCE, CheckKind.OFFLOADING, CheckKind.NOT_ALL_CARGO_LOADED);
    }

    // lateral balance

    private static LoadCheck lateralBalance(Load load) {
        return load.check(FLOOR_PLAN, SOFT_RULES).stream()
                .filter(c -> c.kind() == CheckKind.LATERAL_BALANCE).findFirst().orElseThrow();
    }

    private static Load loadWithLeftAndRight(ULD left, ULD right) {
        Load load = openLoad();
        load.book(left, NOW);
        load.book(right, NOW);
        load.place(left.code(), L1, FLOOR_PLAN, NOW);
        load.place(right.code(), R1, FLOOR_PLAN, NOW);
        return load;
    }

    @Test
    void lateralBalanceExactlyAtLimitPasses() {
        Load load = loadWithLeftAndRight(uld(CODE_A, "1000", ATH), uld(CODE_B, "1100", ATH));
        assertThat(lateralBalance(load).result()).isEqualTo(Result.PASSED);
    }

    @Test
    void lateralBalanceJustOverLimitFails() {
        Load load = loadWithLeftAndRight(uld(CODE_A, "1000", ATH), uld(CODE_B, "1101", ATH));
        LoadCheck check = lateralBalance(load);
        assertThat(check.result()).isEqualTo(Result.FAILED);
        assertThat(check.isBlocking()).isTrue();
    }

    @Test
    void lateralBalanceReportShowsBothSidesAndMaximum() {
        Load load = loadWithLeftAndRight(uld(CODE_A, "1000", ATH), uld(CODE_B, "1101", ATH));
        assertThat(lateralBalance(load).explanation())
                .contains("1000", "1101", "1100", "kg");
    }

    @Test
    void lateralBalanceConvertsPoundsBeforeComparing() {
        ULD leftKg = uld(CODE_A, "1000", ATH);
        ULD rightLb = ULD.of(CODE_B, new Weight(new BigDecimal("2425"), WeightUnit.LB), ATH); // ≈ 1099.96 kg
        Load load = loadWithLeftAndRight(leftKg, rightLb);
        assertThat(lateralBalance(load).result()).isEqualTo(Result.PASSED);
    }

    @Test
    void lateralBalanceWithCargoOnOneSideOnlyFails() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.place(CODE_A, L1, FLOOR_PLAN, NOW);
        assertThat(lateralBalance(load).result()).isEqualTo(Result.FAILED);
    }

    @Test
    void lateralBalanceIgnoresNoseAndTail() {
        Load load = openLoad();
        load.book(uld(CODE_A, "5000", ATH), NOW);
        load.place(CODE_A, N1, FLOOR_PLAN, NOW);
        assertThat(lateralBalance(load).result()).isEqualTo(Result.PASSED);
    }

    // offloading

    private static LoadCheck offloading(Load load, LoadRules rules) {
        return load.check(FLOOR_PLAN, rules).stream()
                .filter(c -> c.kind() == CheckKind.OFFLOADING).findFirst().orElseThrow();
    }

    /** Books and places 1000 kg ULDs, given as triples: code, destination, position code. */
    private static Load loadWith(Object... codeDestinationPosition) {
        Load load = openLoad();
        for (int i = 0; i < codeDestinationPosition.length; i += 3) {
            ULDCode code = (ULDCode) codeDestinationPosition[i];
            load.book(uld(code, "1000", (Airport) codeDestinationPosition[i + 1]), NOW);
            load.place(code, FloorPlans.pos((String) codeDestinationPosition[i + 2]), FLOOR_PLAN, NOW);
        }
        return load;
    }

    @Test
    void offloadingFailsWhenOtherDestinationIsOnTheRouteToTheDoor() {
        Load load = loadWith(CODE_A, ATH, "TL", CODE_B, CDG, "SL");
        LoadCheck check = offloading(load, HARD_RULES);
        assertThat(check.result()).isEqualTo(Result.FAILED);
        assertThat(check.explanation()).contains("AKE11111AA on TL");
    }

    @Test
    void offloadingPassesWhenSameDestinationIsOnTheRoute() {
        Load load = loadWith(CODE_A, ATH, "TL", CODE_B, ATH, "SL");
        assertThat(offloading(load, HARD_RULES).result()).isEqualTo(Result.PASSED);
    }

    @Test
    void offloadingIgnoresBlockersOnTheOtherSide() {
        Load load = loadWith(CODE_A, ATH, "TR", CODE_B, CDG, "SL");
        assertThat(offloading(load, HARD_RULES).result()).isEqualTo(Result.PASSED);
    }

    @Test
    void offloadingFailsWhenRightSideUldIsBlockedAtTheDoor() {
        Load load = loadWith(CODE_A, ATH, "TR", CODE_B, CDG, "RL");
        assertThat(offloading(load, HARD_RULES).result()).isEqualTo(Result.FAILED);
    }

    @Test
    void otherDestinationUldsAreNeverCheckedThemselves() {
        Load load = loadWith(CODE_A, CDG, "TL", CODE_B, CDG, "SL");
        assertThat(offloading(load, HARD_RULES).result()).isEqualTo(Result.PASSED);
    }

    @Test
    void noseFailsWhenEitherCPositionHoldsOtherDestination() {
        Load load = loadWith(CODE_A, ATH, "A1", CODE_B, CDG, "CR");
        assertThat(offloading(load, HARD_RULES).result()).isEqualTo(Result.FAILED);
    }

    @Test
    void noseCanUseTheOtherSideWhenOneRouteIsBlocked() {
        Load load = loadWith(CODE_A, ATH, "A1", CODE_B, CDG, "DL");
        assertThat(offloading(load, HARD_RULES).result()).isEqualTo(Result.PASSED);
    }

    @Test
    void tailFailsWhenBothRoutesAreBlocked() {
        Load load = loadWith(CODE_A, ATH, "X", CODE_B, CDG, "SL", CODE_C, CDG, "SR");
        assertThat(offloading(load, HARD_RULES).result()).isEqualTo(Result.FAILED);
    }

    @Test
    void tailPassesWhenOneRouteIsClear() {
        Load load = loadWith(CODE_A, ATH, "X", CODE_B, CDG, "SL");
        assertThat(offloading(load, HARD_RULES).result()).isEqualTo(Result.PASSED);
    }

    @Test
    void offloadingSeverityFollowsThePolicy() {
        Load load = loadWith(CODE_A, ATH, "TL", CODE_B, CDG, "SL");
        assertThat(offloading(load, HARD_RULES).isBlocking()).isTrue();
        assertThat(offloading(load, SOFT_RULES).isWarning()).isTrue();
    }

    // not all cargo loaded

    @Test
    void unplacedUldFailsNotAllCargoLoadedAsSoftEvenWithHardPolicy() {
        Load load = loadWithOneUnplacedUld();
        List<LoadCheck> checks = load.check(FLOOR_PLAN, HARD_RULES);
        LoadCheck notAllLoaded = checks.stream()
                .filter(c -> c.kind() == CheckKind.NOT_ALL_CARGO_LOADED).findFirst().orElseThrow();
        assertThat(notAllLoaded.result()).isEqualTo(Result.FAILED);
        assertThat(notAllLoaded.severity()).isEqualTo(Severity.SOFT);
    }

    // finalise

    @Test
    void unplacedCargoWithoutConfirmationThrowsAndLeavesLoadOpen() {
        Load load = loadWithOneUnplacedUld();
        assertThatThrownBy(() -> load.finalise(false, FLOOR_PLAN, SOFT_RULES, NOW))
                .isInstanceOf(IllegalStateException.class);
        assertThat(load.status()).isEqualTo(LoadStatus.OPEN);
        assertThat(load.getDomainEvents()).isEmpty();
    }

    @Test
    void confirmedUnplacedCargoFinalisesAndEmitsEventWithOnlyPlacedUlds() {
        Load load = loadWithOneUnplacedUld();
        load.finalise(true, FLOOR_PLAN, SOFT_RULES, NOW);
        assertThat(load.status()).isEqualTo(LoadStatus.FINAL);
        assertThat(load.getDomainEvents()).hasSize(1);
        LoadFinalisedEvent event = (LoadFinalisedEvent) load.getDomainEvents().get(0);
        assertThat(event.flightId()).isEqualTo(FLIGHT_ID);
        assertThat(event.placedUlds()).hasSize(1);
        assertThat(event.placedUlds().get(0).uldCode()).isEqualTo(CODE_A.value());
    }

    @Test
    void unplacedCargoCanBeConfirmedEvenWithHardOffloadingPolicy() {
        Load load = loadWithOneUnplacedUld();
        load.finalise(true, FLOOR_PLAN, HARD_RULES, NOW);
        assertThat(load.status()).isEqualTo(LoadStatus.FINAL);
    }

    @Test
    void hardOffloadingBlocksFinaliseEvenWithConfirmation() {
        // ATH on the nose, CDG on both C positions (balanced, so lateral balance passes)
        Load load = loadWith(CODE_A, ATH, "A1", CODE_B, CDG, "CL", CODE_C, CDG, "CR");
        assertThatThrownBy(() -> load.finalise(true, FLOOR_PLAN, HARD_RULES, NOW))
                .isInstanceOf(IllegalStateException.class);
        assertThat(load.status()).isEqualTo(LoadStatus.OPEN);
    }

    @Test
    void softOffloadingCanBeConfirmed() {
        Load load = loadWith(CODE_A, ATH, "A1", CODE_B, CDG, "CL", CODE_C, CDG, "CR");
        load.finalise(true, FLOOR_PLAN, SOFT_RULES, NOW);
        assertThat(load.status()).isEqualTo(LoadStatus.FINAL);
    }

    @Test
    void placeAfterFinalisedThrows() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.place(CODE_A, N1, FLOOR_PLAN, NOW);
        load.finalise(false, FLOOR_PLAN, SOFT_RULES, NOW);
        assertThatThrownBy(() -> load.place(CODE_A, L1, FLOOR_PLAN, NOW))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void clearDomainEventsEmptiesEventListAfterFinalise() {
        Load load = loadWithOneUnplacedUld();
        load.finalise(true, FLOOR_PLAN, SOFT_RULES, NOW);
        load.clearDomainEvents();
        assertThat(load.getDomainEvents()).isEmpty();
    }

    // updateFlightFacts

    @Test
    void updatingWithDifferentRegistrationClearsPlacements() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.place(CODE_A, L1, FLOOR_PLAN, NOW);
        load.updateFlightFacts(ATH, new AircraftRegistration("OOABC"), LoadingWindow.beforeDeparture(STD, 720, 20));
        assertThat(load.placements()).isEmpty();
        assertThat(load.statusOf(CODE_A)).isEqualTo(ULDStatus.BOOKED);
    }

    @Test
    void updateFlightFactsOnFinalLoadChangesNothing() {
        ULD uldA = uld(CODE_A, "1000", ATH);
        ULDPlacement placement = new ULDPlacement(CODE_A, N1);
        Load load = Load.restore(LoadId.create(), FLIGHT_ID, REG, ATH,
                LoadingWindow.beforeDeparture(STD, 720, 20), LoadStatus.FINAL,
                List.of(uldA), List.of(placement), List.of());
        load.updateFlightFacts(CDG, new AircraftRegistration("OOABC"), LoadingWindow.beforeDeparture(STD, 600, 30));
        assertThat(load.destinationAirport()).isEqualTo(ATH);
        assertThat(load.placements()).containsExactly(placement);
    }

    @Test
    void updateFlightFactsClearsPreviouslyStoredChecks() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.check(FLOOR_PLAN, SOFT_RULES);
        assertThat(load.checks()).isNotEmpty();
        load.updateFlightFacts(ATH, REG, LoadingWindow.beforeDeparture(STD, 720, 20));
        assertThat(load.checks()).isEmpty();
    }

    // immutability

    @Test
    void uldListReturnedByUldsIsUnmodifiable() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        assertThatThrownBy(() -> load.ulds().add(uld(CODE_B, "500", ATH)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
