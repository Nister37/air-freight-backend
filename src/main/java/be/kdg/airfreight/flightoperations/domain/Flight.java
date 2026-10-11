package be.kdg.airfreight.flightoperations.domain;

import be.kdg.airfreight.common.events.FlightTimesChangedEvent;
import be.kdg.airfreight.common.events.FlightLandedEvent;
import be.kdg.airfreight.common.events.FlightDepartedEvent;
import be.kdg.airfreight.common.events.FlightCreatedEvent;
import be.kdg.airfreight.common.events.DomainEvent;
import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.DelayNotFoundException;
import java.time.*;
import java.util.*;

public class Flight {
    private static final int MAX_TOTAL_DELAY_MINUTES = 720;

    private final FlightId id;
    private final Airport arrivalAirport;
    private final AircraftRegistration aircraftRegistration;
    private final Departure departure;
    private final Arrival arrival;
    private boolean finalLoadReceived;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Flight(FlightId id, Airport arrivalAirport, AircraftRegistration aircraftRegistration,
                   Departure departure, Arrival arrival, boolean finalLoadReceived) {
        this.id = Objects.requireNonNull(id);
        this.arrivalAirport = Objects.requireNonNull(arrivalAirport);
        this.aircraftRegistration = Objects.requireNonNull(aircraftRegistration);
        this.departure = Objects.requireNonNull(departure);
        this.arrival = Objects.requireNonNull(arrival);
        if (!departure.scheduledTime().toLocalDate().equals(id.scheduledDepartureDate())
                || !arrival.scheduledTime().isAfter(departure.scheduledTime())) {
            throw new IllegalArgumentException("Scheduled arrival must follow departure and the identity date must match UTC STD");
        }
        this.finalLoadReceived = finalLoadReceived;
    }

    public static Flight announce(FlightSchedule schedule, LocalDate date, LocalDateTime now) {
        Flight flight = new Flight(new FlightId(schedule.flightNumber(), date, schedule.departureAirport()),
                schedule.arrivalAirport(), schedule.aircraftRegistration(), new Departure(schedule.departureOn(date)),
                new Arrival(schedule.arrivalOn(date)), false);
        flight.domainEvents.add(new FlightCreatedEvent(UUID.randomUUID(), now, flight.facts()));
        return flight;
    }

    public static Flight restore(FlightId id, Airport arrivalAirport, AircraftRegistration registration,
                                 Departure departure, Arrival arrival, boolean finalLoadReceived) {
        return new Flight(id, arrivalAirport, registration, departure, arrival, finalLoadReceived);
    }

    public boolean isDeparted() { return departure.isDeparted(); }
    public boolean isLanded() { return arrival.isLanded(); }

    public void registerDepartureDelay(Delay delay, LocalDateTime now) {
        validateNewDelay(delay);
        LocalDateTime oldDeparture = departure.estimatedTime();
        LocalDateTime oldArrival = estimatedArrival();
        departure.registerDelay(delay);
        recordEstimateChanges(oldDeparture, oldArrival, now);
    }

    public void registerArrivalDelay(Delay delay, LocalDateTime now) {
        validateNewDelay(delay);
        LocalDateTime oldDeparture = departure.estimatedTime();
        LocalDateTime oldArrival = estimatedArrival();
        arrival.registerDelay(delay);
        recordEstimateChanges(oldDeparture, oldArrival, now);
    }

    private void validateNewDelay(Delay delay) {
        Objects.requireNonNull(delay);
        if (departure.hasDelay(delay.id()) || arrival.hasDelay(delay.id())) throw new IllegalStateException("Delay already exists");
        validateTotalDelay(totalDelayMinutes() + delay.minutes());
    }

    public void correctDelay(DelayId id, int minutes, String description, LocalDateTime now) {
        Delay old = delay(id);
        Delay corrected = old.corrected(minutes, description);
        validateTotalDelay(totalDelayMinutes() - old.minutes() + corrected.minutes());
        LocalDateTime oldDeparture = departure.estimatedTime();
        LocalDateTime oldArrival = estimatedArrival();
        if (departure.hasDelay(id)) departure.correctDelay(id, minutes, description);
        else arrival.correctDelay(id, minutes, description);
        recordEstimateChanges(oldDeparture, oldArrival, now);
    }

    public void removeDelay(DelayId id, LocalDateTime now) {
        delay(id);
        LocalDateTime oldDeparture = departure.estimatedTime();
        LocalDateTime oldArrival = estimatedArrival();
        if (departure.hasDelay(id)) departure.removeDelay(id);
        else arrival.removeDelay(id);
        recordEstimateChanges(oldDeparture, oldArrival, now);
    }

    private Delay delay(DelayId id) {
        if (departure.hasDelay(id)) return departure.delay(id);
        if (arrival.hasDelay(id)) return arrival.delay(id);
        throw new DelayNotFoundException("Delay does not exist: " + id.value());
    }

    public void confirmFinalLoadReceived() { finalLoadReceived = true; }

    public void recordDeparture(LocalDateTime at, LocalDateTime now, int toleranceMinutes) {
        if (!finalLoadReceived) throw new IllegalStateException("Departure refused: the final-load CPM has not been processed yet");
        departure.recordDeparture(at, now, toleranceMinutes);
        domainEvents.add(new FlightDepartedEvent(UUID.randomUUID(), now, facts()));
    }

    public void recordArrival(LocalDateTime at, LocalDateTime now, int toleranceMinutes) {
        if (!isDeparted()) throw new IllegalStateException("Arrival requires an actual departure first");
        arrival.recordArrival(at, now, toleranceMinutes, departure.actualTime(), departure.totalDelayMinutes());
        domainEvents.add(new FlightLandedEvent(UUID.randomUUID(), now, facts()));
    }

    private long totalDelayMinutes() { return departure.totalDelayMinutes() + arrival.totalDelayMinutes(); }

    private void validateTotalDelay(long minutes) {
        if (minutes > MAX_TOTAL_DELAY_MINUTES) throw new IllegalStateException("A flight delayed more than twelve hours must be rescheduled");
    }

    private LocalDateTime estimatedArrival() { return arrival.estimatedTime(departure.totalDelayMinutes()); }

    private void recordEstimateChanges(LocalDateTime oldDeparture, LocalDateTime oldArrival, LocalDateTime now) {
        boolean departureChanged = !departure.estimatedTime().equals(oldDeparture);
        boolean arrivalChanged = !estimatedArrival().equals(oldArrival);
        if (departureChanged || arrivalChanged) {
            domainEvents.add(new FlightTimesChangedEvent(UUID.randomUUID(), now, facts(), departureChanged, arrivalChanged));
        }
    }

    public FlightFacts facts() {
        FlightStatus status = isLanded() ? FlightStatus.LANDED : isDeparted() ? FlightStatus.DEPARTED : FlightStatus.SCHEDULED;
        List<DelayDetails> delays = new ArrayList<>();
        for (Delay delay : departure.delays()) delays.add(details(delay, DelayPhase.DEPARTURE));
        for (Delay delay : arrival.delays()) delays.add(details(delay, DelayPhase.ARRIVAL));
        return new FlightFacts(id, arrivalAirport, aircraftRegistration, departure.scheduledTime(), arrival.scheduledTime(),
                departure.estimatedTime(), estimatedArrival(), departure.actualTime(), arrival.actualTime(), status, finalLoadReceived, delays);
    }

    private DelayDetails details(Delay delay, DelayPhase phase) {
        return new DelayDetails(delay.id().value(), delay.code().value(), delay.description(), delay.minutes(), phase);
    }

    public List<DomainEvent> getDomainEvents() { return List.copyOf(domainEvents); }
    public void clearDomainEvents() { domainEvents.clear(); }
}
