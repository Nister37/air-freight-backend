package be.kdg.airfreight.simulation.port.in.usecase;

import be.kdg.airfreight.simulation.port.in.command.SimulateCargoBookingsCommand;

import java.util.List;
import java.util.UUID;

public interface SimulateCargoBookingsUseCase {
    List<UUID> simulateCargoBookings(SimulateCargoBookingsCommand command);
}
