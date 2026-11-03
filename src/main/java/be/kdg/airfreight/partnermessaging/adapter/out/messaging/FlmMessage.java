package be.kdg.airfreight.partnermessaging.adapter.out.messaging;

import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FlmMessage(String flightNumber, LocalDate scheduledDepartureDate, String departureAirport,
                         String arrivalAirport, LocalDateTime STD, LocalDateTime STA) {
    public static FlmMessage from(FlightId flightId, Airport arrivalAirport,
                                  LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) {
        return new FlmMessage(flightId.flightNumber().value(), flightId.scheduledDepartureDate(),
                flightId.departureAirport().value(), arrivalAirport.value(),
                scheduledDeparture, scheduledArrival);
    }
}
