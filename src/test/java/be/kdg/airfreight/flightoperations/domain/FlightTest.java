package be.kdg.airfreight.flightoperations.domain;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.DelayDetails;
import be.kdg.airfreight.common.FlightNumber;
import be.kdg.airfreight.common.FlightSchedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FlightTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 11);
    private static final LocalDateTime NOW = DATE.atTime(15, 0);
    private static final int TOLERANCE = 5;

    private Flight flight() {
        return Flight.announce(new FlightSchedule(DATE, DATE, Set.of(DATE.getDayOfWeek()),
                LocalTime.of(10, 0), LocalTime.of(12, 0), new FlightNumber("HJ221"),
                new Airport("BRU"), new Airport("ATH"), new AircraftRegistration("SXBAA")), DATE);
    }

    private Delay delay(int minutes) {
        return new Delay(DelayId.create(), new DelayCode("93"), "Aircraft rotation", minutes);
    }

    @Test
    void estimatesFollowRegisteredCorrectedAndRemovedDelays() {
        Flight flight = flight();
        Delay departure = delay(30);
        Delay arrival = delay(15);

        flight.registerDepartureDelay(departure);
        flight.registerArrivalDelay(arrival);

        assertEquals(DATE.atTime(10, 30), flight.facts().estimatedDeparture());
        assertEquals(DATE.atTime(12, 45), flight.facts().estimatedArrival());
        flight.correctDelay(departure.id(), 45, "Updated rotation");
        assertEquals(DATE.atTime(10, 45), flight.facts().estimatedDeparture());
        assertEquals(DATE.atTime(13, 0), flight.facts().estimatedArrival());
        flight.removeDelay(departure.id());
        flight.removeDelay(arrival.id());
        assertEquals(DATE.atTime(10, 0), flight.facts().estimatedDeparture());
        assertEquals(DATE.atTime(12, 0), flight.facts().estimatedArrival());
        assertEquals(DATE.atTime(10, 0), flight.facts().scheduledDeparture());
        assertEquals(DATE.atTime(12, 0), flight.facts().scheduledArrival());
    }

    @Test
    void departureRequiresTheFinalLoadAndCannotBeRecordedTwice() {
        Flight flight = flight();

        assertThrows(IllegalStateException.class, () -> flight.recordDeparture(DATE.atTime(10, 0), NOW, TOLERANCE));
        assertNull(flight.facts().actualDeparture());
        flight.confirmFinalLoadReceived();
        flight.recordDeparture(DATE.atTime(10, 0), NOW, TOLERANCE);

        assertTrue(flight.isDeparted());
        assertThrows(IllegalStateException.class, () -> flight.recordDeparture(DATE.atTime(10, 0), NOW, TOLERANCE));
    }

    @Test
    void actualTimesAllowTheExactToleranceButRequireADelayBeyondIt() {
        Flight flight = flight();
        flight.confirmFinalLoadReceived();

        assertThrows(IllegalStateException.class, () -> flight.recordDeparture(DATE.atTime(10, 5, 1), NOW, TOLERANCE));
        flight.recordDeparture(DATE.atTime(10, 5), NOW, TOLERANCE);
        assertThrows(IllegalStateException.class, () -> flight.recordArrival(DATE.atTime(12, 5, 1), NOW, TOLERANCE));
        flight.recordArrival(DATE.atTime(12, 5), NOW, TOLERANCE);

        assertEquals(DATE.atTime(10, 5), flight.facts().actualDeparture());
        assertEquals(DATE.atTime(12, 5), flight.facts().actualArrival());
        Flight early = flight();
        early.confirmFinalLoadReceived();
        early.recordDeparture(DATE.atTime(9, 30), NOW, TOLERANCE);
        early.recordArrival(DATE.atTime(11, 30), NOW, TOLERANCE);
        assertEquals(DATE.atTime(9, 30), early.facts().actualDeparture());
        assertEquals(DATE.atTime(11, 30), early.facts().actualArrival());
    }

    @Test
    void actualTimesCannotBeFutureOrBreakTheDepartureArrivalOrder() {
        Flight flight = flight();
        flight.confirmFinalLoadReceived();

        assertThrows(IllegalStateException.class, () -> flight.recordArrival(DATE.atTime(12, 0), NOW, TOLERANCE));
        assertThrows(IllegalStateException.class, () -> flight.recordDeparture(NOW.plusSeconds(1), NOW, TOLERANCE));
        flight.recordDeparture(DATE.atTime(10, 0), NOW, TOLERANCE);
        assertThrows(IllegalStateException.class, () -> flight.recordArrival(DATE.atTime(10, 0), NOW, TOLERANCE));
        assertThrows(IllegalStateException.class, () -> flight.recordArrival(DATE.atTime(9, 59), NOW, TOLERANCE));
        assertThrows(IllegalStateException.class, () -> flight.recordArrival(NOW.plusSeconds(1), NOW, TOLERANCE));
    }

    @Test
    void departureFreezesDepartureDelaysAndLandingFreezesAllDelays() {
        Flight flight = flight();
        Delay departure = delay(10);
        Delay arrival = delay(10);
        flight.registerDepartureDelay(departure);
        flight.registerArrivalDelay(arrival);
        flight.confirmFinalLoadReceived();

        flight.recordDeparture(DATE.atTime(10, 10), NOW, TOLERANCE);

        assertThrows(IllegalStateException.class, () -> flight.registerDepartureDelay(delay(1)));
        assertThrows(IllegalStateException.class, () -> flight.correctDelay(departure.id(), 20, "Correction"));
        assertThrows(IllegalStateException.class, () -> flight.removeDelay(departure.id()));
        Delay extraArrival = delay(5);
        flight.registerArrivalDelay(extraArrival);
        flight.removeDelay(extraArrival.id());
        flight.correctDelay(arrival.id(), 20, "Arrival correction");
        flight.recordArrival(DATE.atTime(12, 30), NOW, TOLERANCE);
        assertTrue(flight.isLanded());
        assertThrows(IllegalStateException.class, () -> flight.registerArrivalDelay(delay(1)));
        assertThrows(IllegalStateException.class, () -> flight.correctDelay(arrival.id(), 30, "Correction"));
        assertThrows(IllegalStateException.class, () -> flight.removeDelay(arrival.id()));
        assertThrows(IllegalStateException.class, () -> flight.recordArrival(DATE.atTime(12, 30), NOW, TOLERANCE));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 721})
    void rejectsDelayDurationsOutsideOneMinuteToTwelveHours(int minutes) {
        assertThrows(IllegalArgumentException.class, () -> delay(minutes));
    }

    @Test
    void combinedDelaysCannotExceedTwelveHours() {
        Flight flight = flight();
        Delay departure = delay(700);
        flight.registerDepartureDelay(departure);
        flight.registerArrivalDelay(delay(20));

        assertThrows(IllegalStateException.class, () -> flight.correctDelay(departure.id(), 701, "Correction"));
        assertThrows(IllegalStateException.class, () -> flight.registerArrivalDelay(delay(1)));

        assertEquals(List.of(700, 20), flight.facts().delays().stream().map(DelayDetails::minutes).toList());
    }

    @Test
    void aDelayAcrossMidnightKeepsTheScheduledFlightIdentity() {
        FlightSchedule schedule = new FlightSchedule(DATE, DATE, Set.of(DATE.getDayOfWeek()),
                LocalTime.of(23, 30), LocalTime.of(2, 0), new FlightNumber("HJ221"),
                new Airport("BRU"), new Airport("ATH"), new AircraftRegistration("SXBAA"));
        Flight flight = Flight.announce(schedule, DATE);

        flight.registerDepartureDelay(delay(60));

        assertEquals(DATE.plusDays(1).atTime(0, 30), flight.facts().estimatedDeparture());
        assertEquals(DATE.plusDays(1).atTime(3, 0), flight.facts().estimatedArrival());
        assertEquals(DATE, flight.facts().id().scheduledDepartureDate());
    }
}
