package be.kdg.airfreight.simulation.adapter.in.rest.req;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightNumber;
import be.kdg.airfreight.simulation.port.in.command.SimulateScheduleCommand;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

public record ScheduleRequest(LocalDate from, LocalDate to, Set<DayOfWeek> weekdays,
                               LocalTime departureTime, LocalTime arrivalTime, String flightNumber,
                               String departureAirport, String arrivalAirport, String aircraftRegistration) {
    public ScheduleRequest {
        if (from == null || to == null || weekdays == null || weekdays.stream().anyMatch(Objects::isNull)
                || departureTime == null || arrivalTime == null || flightNumber == null
                || departureAirport == null || arrivalAirport == null || aircraftRegistration == null) {
            throw new IllegalArgumentException("All schedule fields are required");
        }
    }

    public SimulateScheduleCommand toCommand() {
        return new SimulateScheduleCommand(from, to, weekdays, departureTime, arrivalTime, new FlightNumber(flightNumber),
                new Airport(departureAirport), new Airport(arrivalAirport), new AircraftRegistration(aircraftRegistration));
    }
}
