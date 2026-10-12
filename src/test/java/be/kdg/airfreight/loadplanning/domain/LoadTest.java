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

    private static final Position L1 = new Position(Side.LEFT, new Slot("1"));
    private static final Position R1 = new Position(Side.RIGHT, new Slot("1"));
    private static final Position N1 = new Position(Side.NOSE, new Slot("1"));
    private static final FloorPlan FLOOR_PLAN = new FloorPlan(L1, List.of(L1, R1, N1));

    private static final ULDCode CODE_A = new ULDCode("AKE11111AA");
    private static final ULDCode CODE_B = new ULDCode("AKE22222BB");

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
    void bookingSameUldTwiceWithEquivalentWeightIsIdempotent() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.book(uld(CODE_A, "1000.0", ATH), NOW);
        assertThat(load.ulds()).hasSize(1);
    }

    @Test
    void bookingSameCodeWithDifferentWeightThrows() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        assertThatThrownBy(() -> load.book(uld(CODE_A, "2000", ATH), NOW))
                .isInstanceOf(IllegalStateException.class);
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
        Position unknown = new Position(Side.TAIL, new Slot("9"));
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
    void destinationMismatchProducesBlockingCheck() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", CDG), NOW);
        load.place(CODE_A, L1, FLOOR_PLAN, NOW);
        List<LoadCheck> checks = load.check(SOFT_RULES);
        assertThat(checks).filteredOn(c -> c.kind() == CheckKind.DESTINATION_MATCH)
                .extracting(LoadCheck::isBlocking)
                .containsExactly(true);
    }

    @Test
    void lateralBalancePassesWhenDifferenceIsWithinLimit() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.book(uld(CODE_B, "900", ATH), NOW);
        load.place(CODE_A, L1, FLOOR_PLAN, NOW);
        load.place(CODE_B, R1, FLOOR_PLAN, NOW);
        List<LoadCheck> checks = load.check(SOFT_RULES);
        assertThat(checks).filteredOn(c -> c.kind() == CheckKind.LATERAL_BALANCE)
                .extracting(LoadCheck::result)
                .containsExactly(Result.PASSED);
    }

    @Test
    void lateralBalanceFailsWhenDifferenceExceedsLimit() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.book(uld(CODE_B, "500", ATH), NOW);
        load.place(CODE_A, L1, FLOOR_PLAN, NOW);
        load.place(CODE_B, R1, FLOOR_PLAN, NOW);
        List<LoadCheck> checks = load.check(SOFT_RULES);
        assertThat(checks).filteredOn(c -> c.kind() == CheckKind.LATERAL_BALANCE)
                .extracting(LoadCheck::result)
                .containsExactly(Result.FAILED);
    }

    @Test
    void unplacedUldProducesOffloadingFailedWithRulesSeverity() {
        Load load = loadWithOneUnplacedUld();
        List<LoadCheck> checks = load.check(SOFT_RULES);
        LoadCheck offloading = checks.stream().filter(c -> c.kind() == CheckKind.OFFLOADING).findFirst().orElseThrow();
        assertThat(offloading.result()).isEqualTo(Result.FAILED);
        assertThat(offloading.severity()).isEqualTo(Severity.SOFT);
    }

    // finalise

    @Test
    void softOffloadingWithoutConfirmationThrowsAndLeavesLoadOpen() {
        Load load = loadWithOneUnplacedUld();
        assertThatThrownBy(() -> load.finalise(false, SOFT_RULES, NOW))
                .isInstanceOf(IllegalStateException.class);
        assertThat(load.status()).isEqualTo(LoadStatus.OPEN);
        assertThat(load.getDomainEvents()).isEmpty();
    }

    @Test
    void softOffloadingWithConfirmationFinalisesAndEmitsEventWithOnlyPlacedUlds() {
        Load load = loadWithOneUnplacedUld();
        load.finalise(true, SOFT_RULES, NOW);
        assertThat(load.status()).isEqualTo(LoadStatus.FINAL);
        assertThat(load.getDomainEvents()).hasSize(1);
        LoadFinalisedEvent event = (LoadFinalisedEvent) load.getDomainEvents().get(0);
        assertThat(event.flightId()).isEqualTo(FLIGHT_ID);
        assertThat(event.placedUlds()).hasSize(1);
        assertThat(event.placedUlds().get(0).uldCode()).isEqualTo(CODE_A.value());
    }

    @Test
    void hardOffloadingBlocksFinaliseEvenWithConfirmation() {
        Load load = loadWithOneUnplacedUld();
        assertThatThrownBy(() -> load.finalise(true, HARD_RULES, NOW))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void placeAfterFinalisedThrows() {
        Load load = openLoad();
        load.book(uld(CODE_A, "1000", ATH), NOW);
        load.place(CODE_A, N1, FLOOR_PLAN, NOW);
        load.finalise(false, SOFT_RULES, NOW);
        assertThatThrownBy(() -> load.place(CODE_A, L1, FLOOR_PLAN, NOW))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void clearDomainEventsEmptiesEventListAfterFinalise() {
        Load load = loadWithOneUnplacedUld();
        load.finalise(true, SOFT_RULES, NOW);
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
        load.check(SOFT_RULES);
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
