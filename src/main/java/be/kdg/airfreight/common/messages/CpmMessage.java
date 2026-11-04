package be.kdg.airfreight.common.messages;

import be.kdg.airfreight.common.FlightId;

import java.time.LocalDate;
import java.util.List;

public record CpmMessage(String flightNumber, LocalDate scheduledDepartureDate, String departureAirport,
                         List<PlacedUld> placedUlds) {
    public CpmMessage {
        placedUlds = List.copyOf(placedUlds);
    }

    public static CpmMessage from(FlightId flight, List<PlacedUld> placedUlds) {
        return new CpmMessage(flight.flightNumber().value(), flight.scheduledDepartureDate(),
                flight.departureAirport().value(), placedUlds);
    }
}
