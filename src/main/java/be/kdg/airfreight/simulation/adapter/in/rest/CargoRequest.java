package be.kdg.airfreight.simulation.adapter.in.rest;

import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.CargoBooking;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.FlightNumber;
import be.kdg.airfreight.common.IataDay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

public record CargoRequest(String flightNumber, LocalDate scheduledDepartureDate, String departureAirport,
                            String uldCode, BigDecimal weight, String unit, String destination) {
    public CargoRequest {
        if (flightNumber == null || scheduledDepartureDate == null || departureAirport == null
                || uldCode == null || weight == null || unit == null || destination == null) {
            throw new IllegalArgumentException("All cargo booking fields are required");
        }
    }

    CargoBooking booking() {
        if (!new IataDay(scheduledDepartureDate.getDayOfMonth()).resolve(LocalDate.now(ZoneOffset.UTC))
                .equals(scheduledDepartureDate)) {
            throw new IllegalArgumentException("Scheduled departure date cannot be represented by the day-only ULD feed at this time");
        }
        return new CargoBooking(UUID.randomUUID(), new FlightId(new FlightNumber(flightNumber),
                scheduledDepartureDate, new Airport(departureAirport)), uldCode, weight, unit, new Airport(destination));
    }
}
