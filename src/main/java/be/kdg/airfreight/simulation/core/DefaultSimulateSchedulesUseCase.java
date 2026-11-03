package be.kdg.airfreight.simulation.core;

import be.kdg.airfreight.simulation.port.in.command.SimulateScheduleCommand;
import be.kdg.airfreight.simulation.port.in.usecase.SimulateSchedulesUseCase;
import be.kdg.airfreight.simulation.port.out.SimulationMessagePublisherPort;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Profile("simulation & !prod & !production")
class DefaultSimulateSchedulesUseCase implements SimulateSchedulesUseCase {
    private final SimulationMessagePublisherPort publisher;

    DefaultSimulateSchedulesUseCase(SimulationMessagePublisherPort publisher) { this.publisher = publisher; }

    @Override
    @PreAuthorize("hasAuthority('ROLE_flight-planner')")
    public List<UUID> simulateSchedules(List<SimulateScheduleCommand> commands) {
        commands = List.copyOf(Objects.requireNonNull(commands, "Schedules are required"));
        if (commands.isEmpty()) throw new IllegalArgumentException("At least one schedule is required");
        return commands.stream().map(command -> {
            UUID messageId = UUID.randomUUID();
            publisher.publishSchedule(messageId, command);
            return messageId;
        }).toList();
    }
}
