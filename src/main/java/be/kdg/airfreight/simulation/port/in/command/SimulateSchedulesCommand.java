package be.kdg.airfreight.simulation.port.in.command;

import be.kdg.airfreight.common.FlightSchedule;

import java.util.List;
import java.util.Objects;

public record SimulateSchedulesCommand(List<FlightSchedule> schedules) {
    public SimulateSchedulesCommand {
        schedules = List.copyOf(Objects.requireNonNull(schedules, "Schedules are required"));
        if (schedules.isEmpty()) throw new IllegalArgumentException("At least one schedule is required");
    }
}
