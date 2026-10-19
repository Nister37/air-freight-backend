package be.kdg.airfreight.simulation.port.in.command;

import be.kdg.airfreight.common.CargoBooking;

import java.util.List;
import java.util.Objects;

public record SimulateCargoBookingsCommand(List<CargoBooking> bookings) {
    public SimulateCargoBookingsCommand {
        bookings = List.copyOf(Objects.requireNonNull(bookings, "Cargo bookings are required"));
        if (bookings.isEmpty()) throw new IllegalArgumentException("At least one cargo booking is required");
    }
}
