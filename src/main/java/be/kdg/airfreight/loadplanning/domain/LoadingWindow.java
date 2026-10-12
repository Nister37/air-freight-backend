package be.kdg.airfreight.loadplanning.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public record LoadingWindow(LocalDateTime opensAt, LocalDateTime closesAt) {
    public LoadingWindow{
        Objects.requireNonNull(opensAt, "opensAt is required");
        Objects.requireNonNull(closesAt, "closesAt is required");
        if (!opensAt.isBefore(closesAt)) {
            throw new IllegalArgumentException("Loading window must open before it closes");
        }
    }

    public static LoadingWindow beforeDeparture(LocalDateTime departure, int openingMinutes, int closingMinutes) {
        return new LoadingWindow(departure.minusMinutes(openingMinutes), departure.minusMinutes(closingMinutes));
    }

    public boolean isOpenAt(LocalDateTime now) {
        return !now.isBefore(opensAt) && now.isBefore(closesAt);
    }

    public boolean isClosedAt(LocalDateTime now) {
        return !now.isBefore(closesAt);
    }
}
