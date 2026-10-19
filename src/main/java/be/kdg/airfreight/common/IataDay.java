package be.kdg.airfreight.common;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.stream.Stream;

/** The day-only flight date carried by IATA feed messages. */
public record IataDay(int value) {
    public IataDay {
        if (value < 1 || value > 31) throw new IllegalArgumentException("Invalid IATA day of month");
    }

    public LocalDate resolve(LocalDate readOn) {
        YearMonth month = YearMonth.from(readOn);
        YearMonth nearest = Stream.of(month.minusMonths(1), month, month.plusMonths(1))
                .min(Comparator.comparingLong((YearMonth candidate) -> Math.abs(ChronoUnit.DAYS.between(
                        readOn, candidate.atDay(1).plusDays(value - 1))))
                        .thenComparing(Comparator.reverseOrder())).orElseThrow();
        if (!nearest.isValidDay(value)) throw new IllegalArgumentException("IATA day does not exist in the inferred month");
        return nearest.atDay(value);
    }
}
