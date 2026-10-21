package be.kdg.airfreight.simulation.port.out;

import be.kdg.airfreight.common.CargoBooking;
import be.kdg.airfreight.common.FlightSchedule;

import java.util.UUID;

public interface SimulationMessagePublisherPort {
    void publishSchedule(UUID messageId, FlightSchedule schedule);
    void publishCargo(CargoBooking booking);
}
