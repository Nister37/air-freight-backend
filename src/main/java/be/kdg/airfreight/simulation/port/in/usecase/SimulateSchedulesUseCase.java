package be.kdg.airfreight.simulation.port.in.usecase;

import be.kdg.airfreight.simulation.port.in.command.SimulateScheduleCommand;

import java.util.List;
import java.util.UUID;

public interface SimulateSchedulesUseCase {
    List<UUID> simulateSchedules(List<SimulateScheduleCommand> commands);
}
