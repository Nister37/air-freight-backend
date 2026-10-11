package be.kdg.airfreight.partnermessaging.adapter.out.formatting;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.*;

/** The derived dates belong to this value and are not recomputed on redelivery. */
public record MvtTelegram(FlightNumber flightNumber, LocalDate scheduledDepartureDate,
                          AircraftRegistration aircraftRegistration, Airport airport,
                          MessageKind kind, LocalDateTime movementTime) {
    private static final Pattern TEXT = Pattern.compile(
            "MVT\\r?\\n([A-Z0-9]+)/(\\d{2})\\.([A-Z0-9-]+)\\.([A-Z]{3})\\r?\\n(ED|EA|AD|AA)(\\d{2})(\\d{2})");

    public MvtTelegram {
        Objects.requireNonNull(flightNumber);
        Objects.requireNonNull(scheduledDepartureDate);
        Objects.requireNonNull(aircraftRegistration);
        Objects.requireNonNull(airport);
        Objects.requireNonNull(kind);
        Objects.requireNonNull(movementTime);
        if (!kind.isTelegram()) throw new IllegalArgumentException("MVT requires ED, EA, AD or AA");
    }

    public String toText() {
        return "MVT\n" + flightNumber.value() + "/" + String.format(Locale.ROOT, "%02d", scheduledDepartureDate.getDayOfMonth())
                + "." + aircraftRegistration.value() + "." + airport.value() + "\n"
                + kind.name().substring(4) + DateTimeFormatter.ofPattern("HHmm").format(movementTime);
    }

    public static MvtTelegram parse(String text, LocalDateTime readAt) {
        Matcher matcher = TEXT.matcher(Objects.requireNonNull(text));
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid three-line IATA MVT telegram");
        LocalDate readingDate = readAt.toLocalDate();
        int day = Integer.parseInt(matcher.group(2));
        int hour = Integer.parseInt(matcher.group(6));
        int minute = Integer.parseInt(matcher.group(7));
        if (hour > 23 || minute > 59) throw new IllegalArgumentException("Invalid MVT time");
        LocalTime time = LocalTime.of(hour, minute);
        if (day < 1 || day > 31) throw new IllegalArgumentException("Invalid day of month");
        YearMonth readingMonth = YearMonth.from(readingDate);
        // Infer the nearest month first, then validate its day rather than normalising e.g. 30 February.
        YearMonth inferredMonth = java.util.stream.Stream.of(readingMonth.minusMonths(1), readingMonth, readingMonth.plusMonths(1))
                .min(Comparator.comparingLong((YearMonth month) -> Math.abs(ChronoUnit.DAYS.between(
                        readingDate, month.atDay(1).plusDays(day - 1))))
                .thenComparing(Comparator.reverseOrder()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid day of month"));
        if (!inferredMonth.isValidDay(day)) throw new IllegalArgumentException("MVT day does not exist in the inferred month");
        LocalDate flightDate = inferredMonth.atDay(day);
        LocalDateTime movement = java.util.stream.Stream.of(readingDate.minusDays(1), readingDate, readingDate.plusDays(1))
                .map(date -> date.atTime(time))
                .min(Comparator.comparingLong((LocalDateTime timeCandidate) -> Math.abs(ChronoUnit.MILLIS.between(readAt, timeCandidate)))
                        .thenComparing(Comparator.reverseOrder())).orElseThrow();
        return new MvtTelegram(new FlightNumber(matcher.group(1)), flightDate,
                new AircraftRegistration(matcher.group(3)), new Airport(matcher.group(4)),
                MessageKind.valueOf("MVT_" + matcher.group(5)), movement);
    }
}
