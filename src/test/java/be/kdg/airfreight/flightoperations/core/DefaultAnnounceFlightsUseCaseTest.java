package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.common.FlightNumber;
import be.kdg.airfreight.common.FlightSchedule;
import be.kdg.airfreight.flightoperations.domain.FlightPolicy;
import be.kdg.airfreight.common.events.FlightCreatedEvent;
import be.kdg.airfreight.flightoperations.domain.Flight;
import be.kdg.airfreight.flightoperations.port.out.FlightLoadPort;
import be.kdg.airfreight.flightoperations.port.out.FlightUpdatePort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultAnnounceFlightsUseCaseTest {
    private final FlightLoadPort loadPort = mock(FlightLoadPort.class);
    private final FlightUpdatePort updatePort = mock(FlightUpdatePort.class);
    private final DefaultAnnounceFlightsUseCase useCase =
            new DefaultAnnounceFlightsUseCase(new FlightAnnouncement(loadPort, List.of(updatePort),
                    new FlightPolicyReader(() -> Optional.of(FlightPolicy.defaults()))));
    private final LocalDate monday = LocalDate.now(ZoneOffset.UTC).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

    @Test
    void announcesMatchingWeekdaysIncludingBothEndsOfThePeriod() {
        FlightSchedule schedule = schedule(monday, monday.plusDays(6));
        when(loadPort.load(any())).thenReturn(Optional.empty());

        List<FlightFacts> flights = useCase.announceFlights(schedule);

        assertEquals(List.of(monday, monday.plusDays(2), monday.plusDays(6)),
                flights.stream().map(flight -> flight.id().scheduledDepartureDate()).toList());
        ArgumentCaptor<Flight> saved = ArgumentCaptor.forClass(Flight.class);
        verify(updatePort, times(3)).update(saved.capture());
        for (Flight flight : saved.getAllValues()) {
            assertEquals(flight.facts().id().scheduledDepartureDate().plusDays(1).atTime(1, 0), flight.facts().scheduledArrival());
            FlightCreatedEvent event = assertInstanceOf(FlightCreatedEvent.class, flight.getDomainEvents().getFirst());
            assertEquals(flight.facts(), event.flight());
        }
    }

    @Test
    void anExistingFlightIsNotAnnouncedAgain() {
        FlightSchedule schedule = schedule(monday, monday);
        Flight existing = Flight.announce(schedule, monday);
        when(loadPort.load(existing.facts().id())).thenReturn(Optional.of(existing));

        List<FlightFacts> flights = useCase.announceFlights(schedule);

        assertTrue(flights.isEmpty());
        verifyNoInteractions(updatePort);
    }

    private FlightSchedule schedule(LocalDate from, LocalDate to) {
        return new FlightSchedule(from, to, Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY),
                LocalTime.of(23, 0), LocalTime.of(1, 0), new FlightNumber("HJ221"),
                new Airport("BRU"), new Airport("ATH"), new AircraftRegistration("SXBAA"));
    }
}
