package be.kdg.airfreight.simulation.port.out;

import be.kdg.airfreight.simulation.port.in.command.SimulateCargoBookingCommand;
import be.kdg.airfreight.simulation.port.in.command.SimulateScheduleCommand;

import java.util.UUID;

public interface SimulationMessagePublisherPort {
    void publishSchedule(UUID messageId, SimulateScheduleCommand command);
    void publishCargo(SimulateCargoBookingCommand command);
}
