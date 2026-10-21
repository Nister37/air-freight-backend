package be.kdg.airfreight.partnermessaging.adapter.out.messaging;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.common.FlightNumber;
import be.kdg.airfreight.common.IataDay;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MvtTelegramFormatter {
    private static final Pattern TEXT = Pattern.compile(
            "MVT\\r?\\n([A-Z0-9]+)/(\\d{2})\\.([A-Z0-9-]+)\\.([A-Z]{3})\\r?\\n([A-Z]{2})([\\s\\S]*)");

    private final Map<String, MvtMessageHandler> handlersByCode;
    private final Map<MessageKind, MvtMessageHandler> handlersByKind;

    public MvtTelegramFormatter(List<MvtMessageHandler> handlers) {
        handlersByCode = handlers.stream().collect(Collectors.toUnmodifiableMap(MvtMessageHandler::code, handler -> handler));
        handlersByKind = handlers.stream().collect(Collectors.toUnmodifiableMap(MvtMessageHandler::kind, handler -> handler));
    }

    public String toText(FlightFacts flight, MessageKind kind, LocalDateTime movementTime) {
        MvtMessageHandler handler = handlerFor(kind);
        return toText(new MvtTelegram(flight.id().flightNumber(), flight.id().scheduledDepartureDate(),
                flight.aircraftRegistration(), handler.airport(flight), kind, movementTime));
    }

    public String toText(MvtTelegram telegram) {
        MvtMessageHandler handler = handlerFor(telegram.kind());
        return "MVT\n" + telegram.flightNumber().value() + "/"
                + String.format(Locale.ROOT, "%02d", telegram.scheduledDepartureDate().getDayOfMonth())
                + "." + telegram.aircraftRegistration().value() + "." + telegram.airport().value() + "\n"
                + handler.code() + handler.format(telegram.movementTime());
    }

    public MvtTelegram parse(String text, LocalDateTime readAt) {
        Matcher matcher = TEXT.matcher(Objects.requireNonNull(text));
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid IATA MVT header or message code");
        MvtMessageHandler handler = handlersByCode.get(matcher.group(5));
        if (handler == null) throw new IllegalArgumentException("Unsupported MVT code: " + matcher.group(5));
        LocalDate flightDate = new IataDay(Integer.parseInt(matcher.group(2))).resolve(readAt.toLocalDate());
        return new MvtTelegram(new FlightNumber(matcher.group(1)), flightDate,
                new AircraftRegistration(matcher.group(3)), new Airport(matcher.group(4)),
                handler.kind(), handler.parse(matcher.group(6), readAt));
    }

    private MvtMessageHandler handlerFor(MessageKind kind) {
        MvtMessageHandler handler = handlersByKind.get(kind);
        if (handler == null) throw new IllegalArgumentException("Unsupported MVT message kind: " + kind);
        return handler;
    }
}
