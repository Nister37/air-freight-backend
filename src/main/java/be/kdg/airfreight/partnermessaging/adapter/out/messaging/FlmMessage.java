package be.kdg.airfreight.partnermessaging.adapter.out.messaging;

import be.kdg.airfreight.common.FlightFacts;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FlmMessage(String flightNumber, LocalDate scheduledDepartureDate, String departureAirport,
                         String arrivalAirport, LocalDateTime STD, LocalDateTime STA) {
    public static FlmMessage from(FlightFacts flight) {
        return new FlmMessage(flight.id().flightNumber().value(), flight.id().scheduledDepartureDate(),
                flight.id().departureAirport().value(), flight.arrivalAirport().value(),
                flight.scheduledDeparture(), flight.scheduledArrival());
    }
}
