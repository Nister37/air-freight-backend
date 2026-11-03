package be.kdg.airfreight.flightoperations.domain.vo;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightNumber;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

public record FlightSchedule(LocalDate from, LocalDate to, Set<DayOfWeek> weekdays,
                             LocalTime departureTime, LocalTime arrivalTime, FlightNumber flightNumber,
                             Airport departureAirport, Airport arrivalAirport,
                             AircraftRegistration aircraftRegistration) {
    public FlightSchedule {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        Objects.requireNonNull(departureTime);
        Objects.requireNonNull(arrivalTime);
        Objects.requireNonNull(flightNumber);
        Objects.requireNonNull(departureAirport);
        Objects.requireNonNull(arrivalAirport);
        Objects.requireNonNull(aircraftRegistration);
        weekdays = Set.copyOf(weekdays);
        if (from.isAfter(to) || weekdays.isEmpty()) {
            throw new IllegalArgumentException("Schedule requires an inclusive date range and at least one weekday");
        }
    }
}
