package be.kdg.airfreight.loadplanning.domain;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.FlightNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LoadTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 11);
    private static final LocalDateTime STD = DATE.atTime(15, 0);
    private static final LocalDateTime NOW = DATE.atTime(10, 0);
    private static final FlightId FLIGHT_ID = new FlightId(new FlightNumber("HJ221"), DATE, new Airport("BRU"));
    private static final FloorPlan FLOOR_PLAN = FloorPlans.B747_400F;
    private static final LoadRules HARD_RULES = new LoadRules(Severity.HARD, new BigDecimal("10"));
    private static final LoadRules SOFT_RULES = new LoadRules(Severity.SOFT, new BigDecimal("10"));
    private static final ULDCode FIRST = new ULDCode("AKE11111AA");
    private static final ULDCode SECOND = new ULDCode("AKE22222BB");
    private static final ULDCode THIRD = new ULDCode("AKE33333CC");
    private final Load load = Load.open(LoadId.create(), FLIGHT_ID, new AircraftRegistration("SXBAA"),
            new Airport("ATH"), LoadingWindow.beforeDeparture(STD, 720, 20));

    private ULD uld(ULDCode code, String amount, WeightUnit unit, String destination) {
        return ULD.of(code, new Weight(new BigDecimal(amount), unit), new Airport(destination));
    }

    private void place(ULDCode code, String amount, String destination, String position) {
        load.book(uld(code, amount, WeightUnit.KG, destination), NOW);
        load.place(code, FloorPlans.pos(position), FLOOR_PLAN, NOW);
    }

    private LoadCheck check(CheckKind kind, LoadRules rules) {
        return load.check(FLOOR_PLAN, LoadChecks.standard(rules), NOW).stream()
                .filter(result -> result.kind() == kind).findFirst().orElseThrow();
    }

    @Test
    void loadingWindowOpensFromStdAndClosesFromEtd() {
        LoadingWindow window = LoadingWindow.beforeDeparture(STD, STD.plusHours(1), 720, 20);

        assertEquals(STD.minusHours(12), window.opensAt());
        assertEquals(STD.plusMinutes(40), window.closesAt());
        assertFalse(window.isOpenAt(window.opensAt().minusSeconds(1)));
        assertTrue(window.isOpenAt(window.opensAt()));
        assertTrue(window.isOpenAt(window.closesAt().minusSeconds(1)));
        assertFalse(window.isOpenAt(window.closesAt()));
    }

    @Test
    void placementsRequireAnOpenLoadingWindow() {
        LocalDateTime opening = STD.minusHours(12);
        LocalDateTime closing = STD.minusMinutes(20);
        load.book(uld(FIRST, "1000", WeightUnit.KG, "ATH"), opening.minusMinutes(1));

        assertThrows(IllegalStateException.class, () -> load.place(FIRST, FloorPlans.pos("CL"), FLOOR_PLAN, opening.minusSeconds(1)));
        load.place(FIRST, FloorPlans.pos("CL"), FLOOR_PLAN, opening);

        assertThrows(IllegalStateException.class, () -> load.removePlacement(FIRST, closing));
        assertEquals(ULDStatus.PLACED, load.statusOf(FIRST));
    }

    @Test
    void lateralBalanceAllowsTheExactPercentageLimit() {
        place(FIRST, "1000", "ATH", "CL");
        place(SECOND, "1100", "ATH", "CR");

        assertEquals(Result.PASSED, check(CheckKind.LATERAL_BALANCE, HARD_RULES).result());
    }

    @Test
    void lateralBalanceAboveTheLimitIsAHardFailure() {
        place(FIRST, "1000", "ATH", "CL");
        place(SECOND, "1101", "ATH", "CR");

        LoadCheck result = check(CheckKind.LATERAL_BALANCE, SOFT_RULES);

        assertEquals(Result.FAILED, result.result());
        assertTrue(result.isBlocking());
    }

    @Test
    void lateralBalanceConvertsPoundsBeforeComparingWeights() {
        place(FIRST, "1000", "ATH", "CL");
        load.book(uld(SECOND, "2425", WeightUnit.LB, "ATH"), NOW);
        load.place(SECOND, FloorPlans.pos("CR"), FLOOR_PLAN, NOW);

        assertEquals(Result.PASSED, check(CheckKind.LATERAL_BALANCE, HARD_RULES).result());
    }

    @Test
    void lateralBalanceIgnoresNoseAndTailPositions() {
        place(FIRST, "1000", "ATH", "A1");
        place(SECOND, "5000", "ATH", "X");

        assertEquals(Result.PASSED, check(CheckKind.LATERAL_BALANCE, HARD_RULES).result());
    }

    @Test
    void otherDestinationCargoBlocksTheRouteToTheDoor() {
        place(FIRST, "1000", "ATH", "TL");
        place(SECOND, "1000", "CDG", "SL");

        assertEquals(Result.FAILED, check(CheckKind.OFFLOADING, HARD_RULES).result());
    }

    @Test
    void cargoForTheSameDestinationCanBeUnloadedFirst() {
        place(FIRST, "1000", "ATH", "TL");
        place(SECOND, "1000", "ATH", "SL");

        assertEquals(Result.PASSED, check(CheckKind.OFFLOADING, HARD_RULES).result());
    }

    @Test
    void rightSideCargoMustCrossToTheLeftDoor() {
        place(FIRST, "1000", "ATH", "TR");
        place(SECOND, "1000", "CDG", "RL");

        assertEquals(Result.FAILED, check(CheckKind.OFFLOADING, HARD_RULES).result());
    }

    @Test
    void noseCargoNeedsBothCPositionsForManoeuvring() {
        place(FIRST, "1000", "ATH", "A1");
        place(SECOND, "1000", "CDG", "CR");

        assertEquals(Result.FAILED, check(CheckKind.OFFLOADING, HARD_RULES).result());
    }

    @Test
    void noseCargoCanChooseTheOtherClearRoute() {
        place(FIRST, "1000", "ATH", "A1");
        place(SECOND, "1000", "CDG", "DL");

        assertEquals(Result.PASSED, check(CheckKind.OFFLOADING, HARD_RULES).result());
    }

    @Test
    void tailCargoNeedsBothUPositionsForManoeuvring() {
        place(FIRST, "1000", "ATH", "X");
        place(SECOND, "1000", "CDG", "UL");

        assertEquals(Result.FAILED, check(CheckKind.OFFLOADING, HARD_RULES).result());
    }

    @Test
    void tailCargoNeedsAtLeastOneClearRoute() {
        place(FIRST, "1000", "ATH", "X");
        place(SECOND, "1000", "CDG", "SL");
        place(THIRD, "1000", "CDG", "SR");
        assertEquals(Result.FAILED, check(CheckKind.OFFLOADING, HARD_RULES).result());

        load.removePlacement(THIRD, NOW);

        assertEquals(Result.PASSED, check(CheckKind.OFFLOADING, HARD_RULES).result());
    }

    @Test
    void offloadingOnlyChecksCargoForTheFlightDestination() {
        place(FIRST, "1000", "CDG", "TL");
        place(SECOND, "1000", "CDG", "SL");

        assertEquals(Result.PASSED, check(CheckKind.OFFLOADING, HARD_RULES).result());
    }

    @Test
    void offloadingSeverityFollowsThePolicy() {
        place(FIRST, "1000", "ATH", "TL");
        place(SECOND, "1000", "CDG", "SL");

        assertTrue(check(CheckKind.OFFLOADING, HARD_RULES).isBlocking());
        assertTrue(check(CheckKind.OFFLOADING, SOFT_RULES).isWarning());
    }

    @Test
    void unplacedCargoProducesASoftWarning() {
        load.book(uld(FIRST, "1000", WeightUnit.KG, "ATH"), NOW);

        LoadCheck result = check(CheckKind.NOT_ALL_CARGO_LOADED, HARD_RULES);

        assertEquals(Result.FAILED, result.result());
        assertTrue(result.isWarning());
    }

    @Test
    void allBookedCargoPlacedPassesTheCargoCheck() {
        place(FIRST, "1000", "ATH", "CL");
        place(SECOND, "1000", "ATH", "CR");

        assertEquals(Result.PASSED, check(CheckKind.NOT_ALL_CARGO_LOADED, HARD_RULES).result());
    }

    @Test
    void hardFailuresPreventFinalisationEvenWhenWarningsAreConfirmed() {
        place(FIRST, "1000", "ATH", "CL");
        place(SECOND, "1101", "ATH", "CR");

        assertThrows(IllegalStateException.class, () -> load.finalise(true, FLOOR_PLAN, LoadChecks.standard(SOFT_RULES), NOW));

        assertEquals(LoadStatus.OPEN, load.status());
    }

    @Test
    void softWarningsRequireConfirmationAndTheFinalLoadCannotChange() {
        place(FIRST, "1000", "ATH", "A1");
        load.book(uld(SECOND, "1000", WeightUnit.KG, "ATH"), NOW);
        assertThrows(IllegalStateException.class, () -> load.finalise(false, FLOOR_PLAN, LoadChecks.standard(HARD_RULES), NOW));

        load.finalise(true, FLOOR_PLAN, LoadChecks.standard(HARD_RULES), NOW);

        assertEquals(LoadStatus.FINAL, load.status());
        assertThrows(IllegalStateException.class, () -> load.removePlacement(FIRST, NOW));
    }
}
