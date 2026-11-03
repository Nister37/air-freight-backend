package be.kdg.airfreight.flightoperations.domain.entity;

import be.kdg.airfreight.flightoperations.domain.vo.DelayId;

import be.kdg.airfreight.flightoperations.DelayNotFoundException;
import java.time.LocalDateTime;
import java.util.*;

/** Departure timing and delays within the Flight aggregate. */
public class Departure {
    private final LocalDateTime scheduledTime;
    private LocalDateTime actualTime;
    private final List<Delay> delays;

    public Departure(LocalDateTime scheduledTime) { this(scheduledTime, null, List.of()); }

    private Departure(LocalDateTime scheduledTime, LocalDateTime actualTime, List<Delay> delays) {
        this.scheduledTime = Objects.requireNonNull(scheduledTime);
        this.actualTime = actualTime;
        this.delays = new ArrayList<>(delays);
    }

    public static Departure restore(LocalDateTime scheduledTime, LocalDateTime actualTime, List<Delay> delays) {
        return new Departure(scheduledTime, actualTime, delays);
    }

    public LocalDateTime scheduledTime() { return scheduledTime; }
    public LocalDateTime actualTime() { return actualTime; }
    public boolean isDeparted() { return actualTime != null; }
    public LocalDateTime estimatedTime() { return scheduledTime.plusMinutes(totalDelayMinutes()); }
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
        if (isDeparted()) throw new IllegalStateException("Departure delays cannot change after departure");
    }

    public void recordDeparture(LocalDateTime at, LocalDateTime now, int toleranceMinutes) {
        if (isDeparted()) throw new IllegalStateException("Actual departure is already recorded and cannot change");
        Objects.requireNonNull(at, "Actual time is required");
        if (at.isAfter(now)) throw new IllegalStateException("Actual time cannot be in the future");
        if (at.isAfter(estimatedTime().plusMinutes(toleranceMinutes))) {
            throw new IllegalStateException("Actual time exceeds tolerance: register a delay first");
        }
        actualTime = at;
    }
}
