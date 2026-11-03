package be.kdg.airfreight.flightoperations.domain.aggregate;

import be.kdg.airfreight.flightoperations.domain.entity.Arrival;
import be.kdg.airfreight.flightoperations.domain.entity.Delay;
import be.kdg.airfreight.flightoperations.domain.entity.Departure;

import be.kdg.airfreight.flightoperations.domain.vo.DelayId;
import be.kdg.airfreight.flightoperations.domain.vo.FlightSchedule;

import be.kdg.airfreight.common.events.FlightTimesChangedEvent;
import be.kdg.airfreight.common.events.FlightLandedEvent;
import be.kdg.airfreight.common.events.FlightDepartedEvent;
import be.kdg.airfreight.common.events.FlightCreatedEvent;
import be.kdg.airfreight.common.events.FlightRemovedEvent;
import be.kdg.airfreight.common.events.DomainEvent;
import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.domain.enums.FlightStatus;
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

    public static Flight announce(FlightSchedule schedule, LocalDate date) {
        LocalDate arrivalDate = schedule.arrivalTime().isAfter(schedule.departureTime())
                ? date : date.plusDays(1);
        Flight flight = new Flight(new FlightId(schedule.flightNumber(), date, schedule.departureAirport()),
                schedule.arrivalAirport(), schedule.aircraftRegistration(),
                new Departure(date.atTime(schedule.departureTime())),
                new Arrival(arrivalDate.atTime(schedule.arrivalTime())), false);
        flight.domainEvents.add(new FlightCreatedEvent(flight.id, flight.arrivalAirport,
                flight.departure.scheduledTime(), flight.arrival.scheduledTime()));
        return flight;
    }

    public static Flight restore(FlightId id, Airport arrivalAirport, AircraftRegistration registration,
                                 Departure departure, Arrival arrival, boolean finalLoadReceived) {
        return new Flight(id, arrivalAirport, registration, departure, arrival, finalLoadReceived);
    }

    public void remove() {
        domainEvents.add(new FlightRemovedEvent(id));
    }

    public boolean isDeparted() { return departure.isDeparted(); }
    public boolean isLanded() { return arrival.isLanded(); }

    public void registerDepartureDelay(Delay delay) {
        validateNewDelay(delay);
        LocalDateTime oldDeparture = departure.estimatedTime();
        LocalDateTime oldArrival = estimatedArrival();
        departure.registerDelay(delay);
        recordEstimateChanges(oldDeparture, oldArrival);
    }

    public void registerArrivalDelay(Delay delay) {
        validateNewDelay(delay);
        LocalDateTime oldDeparture = departure.estimatedTime();
        LocalDateTime oldArrival = estimatedArrival();
        arrival.registerDelay(delay);
        recordEstimateChanges(oldDeparture, oldArrival);
    }

    private void validateNewDelay(Delay delay) {
        Objects.requireNonNull(delay);
        if (departure.hasDelay(delay.id()) || arrival.hasDelay(delay.id())) throw new IllegalStateException("Delay already exists");
        validateTotalDelay(totalDelayMinutes() + delay.minutes());
    }

    public void correctDelay(DelayId id, int minutes, String description) {
        Delay old = delay(id);
        Delay corrected = old.corrected(minutes, description);
        validateTotalDelay(totalDelayMinutes() - old.minutes() + corrected.minutes());
        LocalDateTime oldDeparture = departure.estimatedTime();
        LocalDateTime oldArrival = estimatedArrival();
        if (departure.hasDelay(id)) departure.correctDelay(id, minutes, description);
        else arrival.correctDelay(id, minutes, description);
        recordEstimateChanges(oldDeparture, oldArrival);
    }

    public void removeDelay(DelayId id) {
        delay(id);
        LocalDateTime oldDeparture = departure.estimatedTime();
        LocalDateTime oldArrival = estimatedArrival();
        if (departure.hasDelay(id)) departure.removeDelay(id);
        else arrival.removeDelay(id);
        recordEstimateChanges(oldDeparture, oldArrival);
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
        domainEvents.add(new FlightDepartedEvent(id, aircraftRegistration, departure.actualTime()));
    }

    public void recordArrival(LocalDateTime at, LocalDateTime now, int toleranceMinutes) {
        if (!isDeparted()) throw new IllegalStateException("Arrival requires an actual departure first");
        arrival.recordArrival(at, now, toleranceMinutes, departure.actualTime(), departure.totalDelayMinutes());
        domainEvents.add(new FlightLandedEvent(id, arrivalAirport, aircraftRegistration, arrival.actualTime()));
    }

    private long totalDelayMinutes() { return departure.totalDelayMinutes() + arrival.totalDelayMinutes(); }

    private void validateTotalDelay(long minutes) {
        if (minutes > MAX_TOTAL_DELAY_MINUTES) throw new IllegalStateException("A flight delayed more than twelve hours must be rescheduled");
    }

    private LocalDateTime estimatedArrival() { return arrival.estimatedTime(departure.totalDelayMinutes()); }

    private void recordEstimateChanges(LocalDateTime oldDeparture, LocalDateTime oldArrival) {
        boolean departureChanged = !departure.estimatedTime().equals(oldDeparture);
        boolean arrivalChanged = !estimatedArrival().equals(oldArrival);
        if (departureChanged || arrivalChanged) {
            domainEvents.add(new FlightTimesChangedEvent(id, arrivalAirport, aircraftRegistration,
                    departure.estimatedTime(), estimatedArrival(), departureChanged, arrivalChanged));
        }
    }

    public FlightId getId() { return id; }
    public Airport getArrivalAirport() { return arrivalAirport; }
    public AircraftRegistration getAircraftRegistration() { return aircraftRegistration; }
    public LocalDateTime getScheduledDeparture() { return departure.scheduledTime(); }
    public LocalDateTime getScheduledArrival() { return arrival.scheduledTime(); }
    public LocalDateTime getEstimatedDeparture() { return departure.estimatedTime(); }
    public LocalDateTime getEstimatedArrival() { return estimatedArrival(); }
    public LocalDateTime getActualDeparture() { return departure.actualTime(); }
    public LocalDateTime getActualArrival() { return arrival.actualTime(); }
    public FlightStatus getStatus() {
        return isLanded() ? FlightStatus.LANDED : isDeparted() ? FlightStatus.DEPARTED : FlightStatus.SCHEDULED;
    }
    public boolean isFinalLoadReceived() { return finalLoadReceived; }
    public List<Delay> getDepartureDelays() { return departure.delays(); }
    public List<Delay> getArrivalDelays() { return arrival.delays(); }

    public List<DomainEvent> getDomainEvents() { return List.copyOf(domainEvents); }
    public void clearDomainEvents() { domainEvents.clear(); }
}
