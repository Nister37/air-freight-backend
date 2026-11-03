package be.kdg.airfreight.flightoperations.domain.entity;

import be.kdg.airfreight.flightoperations.domain.vo.DelayId;

import be.kdg.airfreight.flightoperations.DelayNotFoundException;
import java.time.LocalDateTime;
import java.util.*;

/** Arrival timing and delays within the Flight aggregate. */
public class Arrival {
    private final LocalDateTime scheduledTime;
    private LocalDateTime actualTime;
    private final List<Delay> delays;

    public Arrival(LocalDateTime scheduledTime) { this(scheduledTime, null, List.of()); }

    private Arrival(LocalDateTime scheduledTime, LocalDateTime actualTime, List<Delay> delays) {
        this.scheduledTime = Objects.requireNonNull(scheduledTime);
        this.actualTime = actualTime;
        this.delays = new ArrayList<>(delays);
    }

    public static Arrival restore(LocalDateTime scheduledTime, LocalDateTime actualTime, List<Delay> delays) {
        return new Arrival(scheduledTime, actualTime, delays);
    }

    public LocalDateTime scheduledTime() { return scheduledTime; }
    public LocalDateTime actualTime() { return actualTime; }
    public boolean isLanded() { return actualTime != null; }
    public LocalDateTime estimatedTime(long departureDelayMinutes) { return scheduledTime.plusMinutes(departureDelayMinutes + totalDelayMinutes()); }
    public long totalDelayMinutes() { return delays.stream().mapToLong(Delay::minutes).sum(); }
    public List<Delay> delays() { return List.copyOf(delays); }
    public boolean hasDelay(DelayId id) { return delays.stream().anyMatch(delay -> delay.id().equals(id)); }

    public Delay delay(DelayId id) {
        return delays.stream().filter(delay -> delay.id().equals(id)).findFirst()
                .orElseThrow(() -> new DelayNotFoundException("Delay does not exist: " + id.value()));
    }

    public void registerDelay(Delay delay) {
        requireMutableDelays();
        if (hasDelay(delay.id())) throw new IllegalStateException("Delay already exists");
        delays.add(delay);
    }

    public void correctDelay(DelayId id, int minutes, String description) {
        requireMutableDelays();
        Delay old = delay(id);
        delays.set(delays.indexOf(old), old.corrected(minutes, description));
    }

    public void removeDelay(DelayId id) {
        requireMutableDelays();
        delays.remove(delay(id));
    }

    private void requireMutableDelays() {
        if (isLanded()) throw new IllegalStateException("Arrival delays cannot change after arrival");
    }

    public void recordArrival(LocalDateTime at, LocalDateTime now, int toleranceMinutes, LocalDateTime actualDeparture, long departureDelayMinutes) {
        if (isLanded()) throw new IllegalStateException("Actual arrival is already recorded and cannot change");
        Objects.requireNonNull(at, "Actual time is required");
        if (!at.isAfter(actualDeparture)) throw new IllegalStateException("Actual arrival must be after actual departure");
        if (at.isAfter(now)) throw new IllegalStateException("Actual time cannot be in the future");
        if (at.isAfter(estimatedTime(departureDelayMinutes).plusMinutes(toleranceMinutes))) {
            throw new IllegalStateException("Actual time exceeds tolerance: register a delay first");
        }
        actualTime = at;
    }
}
