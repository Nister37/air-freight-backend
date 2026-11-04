package be.kdg.airfreight.flightoperations.domain.aggregate;

import be.kdg.airfreight.flightoperations.domain.entity.Arrival;
import be.kdg.airfreight.flightoperations.domain.entity.Delay;
import be.kdg.airfreight.flightoperations.domain.entity.Departure;

import be.kdg.airfreight.flightoperations.domain.vo.DelayId;
import be.kdg.airfreight.flightoperations.domain.vo.DelayCode;
import be.kdg.airfreight.flightoperations.domain.vo.FlightSchedule;

import be.kdg.airfreight.common.events.FlightTimesChangedEvent;
import be.kdg.airfreight.common.events.FlightLandedEvent;
import be.kdg.airfreight.common.events.FlightDepartedEvent;
import be.kdg.airfreight.common.events.FlightCreatedEvent;
import be.kdg.airfreight.common.events.FlightRemovedEvent;
import be.kdg.airfreight.common.events.DomainEvent;
import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.domain.enums.FlightStatus;
import be.kdg.airfreight.flightoperations.domain.enums.DelayEventType;
import be.kdg.airfreight.flightoperations.domain.enums.DelayPhase;
import be.kdg.airfreight.common.events.DelayEvent;
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

    /** Replays delay events in stream order after the supplied snapshot, without publishing them. */
    public static Flight replayDelays(FlightId id, Airport arrivalAirport, AircraftRegistration registration,
                                     Departure departure, Arrival arrival, boolean finalLoadReceived,
                                     List<DelayEvent> history) {
        List<Delay> departureDelays = new ArrayList<>(departure.delays());
        List<Delay> arrivalDelays = new ArrayList<>(arrival.delays());
        for (DelayEvent event : history) {
            replayDelay(DelayPhase.valueOf(event.phase()) == DelayPhase.DEPARTURE ? departureDelays : arrivalDelays, event);
        }
        return restore(id, arrivalAirport, registration,
                Departure.restore(departure.scheduledTime(), departure.actualTime(), departureDelays),
                Arrival.restore(arrival.scheduledTime(), arrival.actualTime(), arrivalDelays), finalLoadReceived);
    }

    private static void replayDelay(List<Delay> delays, DelayEvent event) {
        Delay delay = new Delay(new DelayId(event.delayId()), new DelayCode(event.code()), event.description(), event.minutes());
        DelayEventType type = DelayEventType.valueOf(event.type());
        int index = -1;
        for (int i = 0; i < delays.size(); i++) {
            if (delays.get(i).id().equals(delay.id())) { index = i; break; }
        }
        switch (type) {
            case REGISTERED -> {
                if (index >= 0) throw new IllegalStateException("Duplicate delay in stored history");
                delays.add(delay);
            }
            case CORRECTED, REMOVED -> {
                if (index < 0) throw new IllegalStateException("Missing delay in stored history");
                if (type == DelayEventType.CORRECTED) delays.set(index, delay);
                else delays.remove(index);
            }
        }
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
        recordDelayChange(DelayEventType.REGISTERED, DelayPhase.DEPARTURE, delay);
        recordEstimateChanges(oldDeparture, oldArrival);
    }

    public void registerArrivalDelay(Delay delay) {
        validateNewDelay(delay);
        LocalDateTime oldDeparture = departure.estimatedTime();
        LocalDateTime oldArrival = estimatedArrival();
        arrival.registerDelay(delay);
        recordDelayChange(DelayEventType.REGISTERED, DelayPhase.ARRIVAL, delay);
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
        DelayPhase phase = departure.hasDelay(id) ? DelayPhase.DEPARTURE : DelayPhase.ARRIVAL;
        if (phase == DelayPhase.DEPARTURE) departure.correctDelay(id, minutes, description);
        else arrival.correctDelay(id, minutes, description);
        recordDelayChange(DelayEventType.CORRECTED, phase, corrected);
        recordEstimateChanges(oldDeparture, oldArrival);
    }

    public void removeDelay(DelayId id) {
        Delay removed = delay(id);
        LocalDateTime oldDeparture = departure.estimatedTime();
        LocalDateTime oldArrival = estimatedArrival();
        DelayPhase phase = departure.hasDelay(id) ? DelayPhase.DEPARTURE : DelayPhase.ARRIVAL;
        if (phase == DelayPhase.DEPARTURE) departure.removeDelay(id);
        else arrival.removeDelay(id);
        recordDelayChange(DelayEventType.REMOVED, phase, removed);
        recordEstimateChanges(oldDeparture, oldArrival);
    }

    private void recordDelayChange(DelayEventType type, DelayPhase phase, Delay delay) {
        domainEvents.add(new DelayEvent(type.name(), phase.name(), delay.id().value(),
                delay.code().value(), delay.description(), delay.minutes()));
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
