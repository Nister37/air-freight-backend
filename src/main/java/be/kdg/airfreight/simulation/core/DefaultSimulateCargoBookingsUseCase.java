package be.kdg.airfreight.simulation.core;

import be.kdg.airfreight.simulation.port.in.command.SimulateCargoBookingsCommand;
import be.kdg.airfreight.simulation.port.in.usecase.SimulateCargoBookingsUseCase;
import be.kdg.airfreight.simulation.port.out.SimulationMessagePublisherPort;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Profile("simulation & !prod & !production")
class DefaultSimulateCargoBookingsUseCase implements SimulateCargoBookingsUseCase {
    private final SimulationMessagePublisherPort publisher;

    DefaultSimulateCargoBookingsUseCase(SimulationMessagePublisherPort publisher) { this.publisher = publisher; }

    @Override
    @PreAuthorize("hasAuthority('ROLE_load-planner')")
    public List<UUID> simulateCargoBookings(SimulateCargoBookingsCommand command) {
        return command.bookings().stream().map(booking -> {
            publisher.publishCargo(booking);
            return booking.messageId();
        }).toList();
    }
}
