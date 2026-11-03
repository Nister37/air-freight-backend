package be.kdg.airfreight.simulation.core;

import be.kdg.airfreight.simulation.port.in.command.SimulateCargoBookingCommand;
import be.kdg.airfreight.simulation.port.in.usecase.SimulateCargoBookingsUseCase;
import be.kdg.airfreight.simulation.port.out.SimulationMessagePublisherPort;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Profile("simulation & !prod & !production")
class DefaultSimulateCargoBookingsUseCase implements SimulateCargoBookingsUseCase {
    private final SimulationMessagePublisherPort publisher;

    DefaultSimulateCargoBookingsUseCase(SimulationMessagePublisherPort publisher) { this.publisher = publisher; }

    @Override
    @PreAuthorize("hasAuthority('ROLE_load-planner')")
    public List<UUID> simulateCargoBookings(List<SimulateCargoBookingCommand> commands) {
        commands = List.copyOf(Objects.requireNonNull(commands, "Cargo bookings are required"));
        if (commands.isEmpty()) throw new IllegalArgumentException("At least one cargo booking is required");
        return commands.stream().map(command -> {
            publisher.publishCargo(command);
            return command.messageId();
        }).toList();
    }
}
