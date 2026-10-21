package be.kdg.airfreight.partnermessaging.adapter.out.messaging;

import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** ED, EA, AD and AA share the same UTC HHmm body. */
public class TimedMvtMessageHandler implements MvtMessageHandler {
    private static final Pattern BODY = Pattern.compile("(\\d{2})(\\d{2})");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HHmm");

    private final String code;
    private final MessageKind kind;
    private final Function<FlightFacts, Airport> airport;

    public TimedMvtMessageHandler(String code, MessageKind kind, Function<FlightFacts, Airport> airport) {
        this.code = Objects.requireNonNull(code);
        this.kind = Objects.requireNonNull(kind);
        this.airport = Objects.requireNonNull(airport);
        if (!code.matches("[A-Z]{2}") || !kind.isTelegram()) {
            throw new IllegalArgumentException("Expected a two-letter MVT code and telegram message kind");
        }
    }

    @Override
    public String code() { return code; }

    @Override
    public MessageKind kind() { return kind; }

    @Override
    public Airport airport(FlightFacts flight) { return airport.apply(flight); }

    @Override
    public LocalDateTime parse(String body, LocalDateTime readAt) {
        Matcher matcher = BODY.matcher(body);
        if (!matcher.matches()) throw new IllegalArgumentException("Expected an MVT time in HHmm format");
        int hour = Integer.parseInt(matcher.group(1));
        int minute = Integer.parseInt(matcher.group(2));
        if (hour > 23 || minute > 59) throw new IllegalArgumentException("Invalid MVT time");
        LocalTime time = LocalTime.of(hour, minute);
        return Stream.of(readAt.toLocalDate().minusDays(1), readAt.toLocalDate(), readAt.toLocalDate().plusDays(1))
                .map(date -> date.atTime(time))
                .min(Comparator.comparingLong((LocalDateTime candidate) -> Math.abs(ChronoUnit.MILLIS.between(readAt, candidate)))
                        .thenComparing(Comparator.reverseOrder())).orElseThrow();
    }

    @Override
    public String format(LocalDateTime movementTime) {
        return TIME.format(Objects.requireNonNull(movementTime));
    }
}
