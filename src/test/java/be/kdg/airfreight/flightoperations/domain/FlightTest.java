package be.kdg.airfreight.flightoperations.domain;

import be.kdg.airfreight.common.events.FlightTimesChangedEvent;
import be.kdg.airfreight.common.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class FlightTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 11);
    private static final LocalDateTime NOW = LocalDateTime.parse("2026-10-11T15:00:00");
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
    void derivesEstimatesAndReturnsToScheduleWhenLastDelayIsRemoved() {
        Flight flight = flight();
        Delay departure = delay(30);
        Delay arrival = delay(15);
        flight.clearDomainEvents();
        flight.registerDepartureDelay(departure);
        flight.registerArrivalDelay(arrival);
        assertThat(flight.facts().estimatedDeparture()).isEqualTo(LocalDateTime.parse("2026-10-11T10:30:00"));
        assertThat(flight.facts().estimatedArrival()).isEqualTo(LocalDateTime.parse("2026-10-11T12:45:00"));
        assertThat(flight.getDomainEvents()).extracting(e -> ((FlightTimesChangedEvent) e).departureChanged()).containsExactly(true, false);
        flight.correctDelay(departure.id(), 45, "Corrected explanation");
        assertThat(flight.facts().estimatedArrival()).isEqualTo(LocalDateTime.parse("2026-10-11T13:00:00"));
        flight.removeDelay(departure.id());
        flight.removeDelay(arrival.id());
        assertThat(flight.facts().estimatedDeparture()).isEqualTo(flight.facts().scheduledDeparture());
        assertThat(flight.facts().estimatedArrival()).isEqualTo(flight.facts().scheduledArrival());
    }

    @Test
    void descriptionCorrectionDoesNotGenerateMovementMessages() {
        Flight flight = flight();
        Delay delay = delay(15);
        flight.registerDepartureDelay(delay);
        flight.clearDomainEvents();
        flight.correctDelay(delay.id(), 15, "Corrected description");
        assertThat(flight.getDomainEvents()).isEmpty();
    }

    @Test
    void departureRequiresProcessedCpmAndSucceedsOnRetry() {
        Flight flight = flight();
        LocalDateTime departure = LocalDateTime.parse("2026-10-11T10:00:00");
        assertThatThrownBy(() -> flight.recordDeparture(departure, NOW, TOLERANCE))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("CPM");
        assertThat(flight.facts().actualDeparture()).isNull();
        flight.confirmFinalLoadReceived();
        flight.confirmFinalLoadReceived();
        flight.recordDeparture(departure, NOW, TOLERANCE);
        assertThat(flight.isDeparted()).isTrue();
        assertThat(flight.isLanded()).isFalse();
        assertThatThrownBy(() -> flight.recordDeparture(departure, NOW, TOLERANCE)).hasMessageContaining("cannot change");
    }

    @Test
    void acceptsExactToleranceAndEarlierTimesButRequiresDelayBeyondTolerance() {
        Flight flight = flight();
        flight.confirmFinalLoadReceived();
        assertThatThrownBy(() -> flight.recordDeparture(LocalDateTime.parse("2026-10-11T10:05:01"), NOW, TOLERANCE))
                .hasMessageContaining("register a delay");
        flight.recordDeparture(LocalDateTime.parse("2026-10-11T10:05:00"), NOW, TOLERANCE);
        Flight early = flight();
        early.confirmFinalLoadReceived();
        early.recordDeparture(LocalDateTime.parse("2026-10-11T09:30:00"), NOW, TOLERANCE);
        assertThat(early.facts().actualDeparture()).isBefore(early.facts().scheduledDeparture());
    }

    @Test
    void rejectsFutureActualTimesAndArrivalBeforeDeparture() {
        Flight flight = flight();
        flight.confirmFinalLoadReceived();
        assertThatThrownBy(() -> flight.recordDeparture(NOW.plusSeconds(1), NOW, TOLERANCE)).hasMessageContaining("future");
        assertThatThrownBy(() -> flight.recordArrival(NOW, NOW, TOLERANCE)).hasMessageContaining("departure first");
        flight.recordDeparture(LocalDateTime.parse("2026-10-11T10:00:00"), NOW, TOLERANCE);
        assertThatThrownBy(() -> flight.recordArrival(flight.facts().actualDeparture(), NOW, TOLERANCE)).hasMessageContaining("after");
        assertThatThrownBy(() -> flight.recordArrival(LocalDateTime.parse("2026-10-11T12:06:00"), NOW, TOLERANCE)).hasMessageContaining("delay");
    }

    @Test
    void departureFreezesDepartureDelaysAndLandingFreezesAllDelays() {
        Flight flight = flight();
        Delay departure = delay(10);
        Delay arrival = delay(10);
        flight.registerDepartureDelay(departure);
        flight.registerArrivalDelay(arrival);
        flight.confirmFinalLoadReceived();
        flight.recordDeparture(LocalDateTime.parse("2026-10-11T10:10:00"), NOW, TOLERANCE);
        assertThatThrownBy(() -> flight.registerDepartureDelay(delay(1))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> flight.correctDelay(departure.id(), 20, "Correction")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> flight.removeDelay(departure.id())).isInstanceOf(IllegalStateException.class);
        flight.correctDelay(arrival.id(), 20, "Arrival correction");
        flight.recordArrival(LocalDateTime.parse("2026-10-11T12:30:00"), NOW, TOLERANCE);
        assertThat(flight.isLanded()).isTrue();
        assertThatThrownBy(() -> flight.registerArrivalDelay(delay(1))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> flight.correctDelay(arrival.id(), 30, "Correction")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> flight.removeDelay(arrival.id())).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> flight.recordArrival(NOW, NOW, TOLERANCE)).hasMessageContaining("cannot change");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 721})
    void rejectsInvalidDelayDuration(int minutes) {
        assertThatThrownBy(() -> delay(minutes)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void midnightDelayDoesNotChangeFlightIdentity() {
        FlightSchedule overnight = new FlightSchedule(DATE, DATE, Set.of(DATE.getDayOfWeek()),
                LocalTime.of(23, 30), LocalTime.of(2, 0), new FlightNumber("HJ221"),
                new Airport("BRU"), new Airport("ATH"), new AircraftRegistration("SXBAA"));
        Flight flight = Flight.announce(overnight, DATE);
        flight.registerDepartureDelay(delay(60));
        assertThat(flight.facts().estimatedDeparture().toLocalDate()).isEqualTo(DATE.plusDays(1));
        assertThat(flight.facts().id().scheduledDepartureDate()).isEqualTo(DATE);
    }

    @Test
    void totalDelayCannotExceedTwelveHoursAndRefusalDoesNotMutateTheAggregate() {
        Flight flight = flight();
        Delay departure = delay(700);
        flight.registerDepartureDelay(departure);
        flight.registerArrivalDelay(delay(20));
        assertThatThrownBy(() -> flight.correctDelay(departure.id(), 701, "Correction")).hasMessageContaining("rescheduled");
        assertThatThrownBy(() -> flight.registerArrivalDelay(delay(1))).hasMessageContaining("rescheduled");
        assertThat(flight.facts().delays()).extracting(DelayDetails::minutes).containsExactly(700, 20);
    }

    @Test
    void aDelayCannotBelongToBothDepartureAndArrival() {
        Flight flight = flight();
        Delay delay = delay(10);
        flight.registerDepartureDelay(delay);
        flight.clearDomainEvents();
        assertThatThrownBy(() -> flight.registerArrivalDelay(delay)).hasMessageContaining("already exists");
        assertThat(flight.facts().delays()).extracting(DelayDetails::phase).containsExactly(DelayPhase.DEPARTURE);
        assertThat(flight.getDomainEvents()).isEmpty();
    }
}
