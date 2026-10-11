package be.kdg.airfreight.operationscoordination.adapter.in.listeners;

import be.kdg.airfreight.common.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

record UldMessage(UUID messageId, String flightNumber, LocalDate scheduledDepartureDate,
                  String departureAirport, String uldCode, BigDecimal weight, String unit, String destination) {
    CargoBooking booking() {
        return new CargoBooking(messageId,
                new FlightId(new FlightNumber(flightNumber), scheduledDepartureDate, new Airport(departureAirport)),
                uldCode, weight, unit, new Airport(destination));
    }
}
