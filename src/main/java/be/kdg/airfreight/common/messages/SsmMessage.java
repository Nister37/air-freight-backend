package be.kdg.airfreight.common.messages;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record SsmMessage(UUID messageId, LocalDate from, LocalDate to, Set<DayOfWeek> weekdays,
                         LocalTime departureTime, LocalTime arrivalTime, String flightNumber,
                         String departureAirport, String arrivalAirport, String aircraftRegistration) { }
