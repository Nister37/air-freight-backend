package be.kdg.airfreight.simulation.port.in.usecase;

import be.kdg.airfreight.simulation.port.in.command.SimulateSchedulesCommand;

import java.util.List;
import java.util.UUID;

public interface SimulateSchedulesUseCase {
    List<UUID> simulateSchedules(SimulateSchedulesCommand command);
}
