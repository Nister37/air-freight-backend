package be.kdg.airfreight.simulation.port.in.usecase;

import be.kdg.airfreight.simulation.port.in.command.SimulateCargoBookingCommand;

import java.util.List;
import java.util.UUID;

public interface SimulateCargoBookingsUseCase {
    List<UUID> simulateCargoBookings(List<SimulateCargoBookingCommand> commands);
}
