package be.kdg.airfreight.common;

import java.time.*;
import java.util.*;

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

    public List<LocalDate> operatingDates() {
        return from.datesUntil(to.plusDays(1)).filter(date -> weekdays.contains(date.getDayOfWeek())).toList();
    }

    public LocalDateTime departureOn(LocalDate date) {
        return date.atTime(departureTime);
    }

    public LocalDateTime arrivalOn(LocalDate date) {
        LocalDate arrivalDate = arrivalTime.isAfter(departureTime) ? date : date.plusDays(1);
        return arrivalDate.atTime(arrivalTime);
    }
}
