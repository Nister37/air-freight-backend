package be.kdg.airfreight.flightoperations.adapter.in.listeners;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.port.in.command.ScheduleFeedCommand;
import java.time.*;
import java.util.*;

/** Wire mapping isolated here until the lecturer's fixed JSON schema is supplied. */
record SsmMessage(UUID messageId, LocalDate from, LocalDate to, Set<DayOfWeek> weekdays,
                  LocalTime departureTime, LocalTime arrivalTime, String flightNumber,
                  String departureAirport, String arrivalAirport, String aircraftRegistration) {
    ScheduleFeedCommand command() {
        return new ScheduleFeedCommand(messageId, new FlightSchedule(from, to, weekdays, departureTime, arrivalTime,
                new FlightNumber(flightNumber), new Airport(departureAirport), new Airport(arrivalAirport),
                new AircraftRegistration(aircraftRegistration)));
    }
}
