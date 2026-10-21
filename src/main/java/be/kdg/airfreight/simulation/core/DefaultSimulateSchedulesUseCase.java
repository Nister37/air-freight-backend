package be.kdg.airfreight.simulation.core;

import be.kdg.airfreight.simulation.port.in.command.SimulateSchedulesCommand;
import be.kdg.airfreight.simulation.port.in.usecase.SimulateSchedulesUseCase;
import be.kdg.airfreight.simulation.port.out.SimulationMessagePublisherPort;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Profile("simulation & !prod & !production")
class DefaultSimulateSchedulesUseCase implements SimulateSchedulesUseCase {
    private final SimulationMessagePublisherPort publisher;

    DefaultSimulateSchedulesUseCase(SimulationMessagePublisherPort publisher) { this.publisher = publisher; }

    @Override
    @PreAuthorize("hasAuthority('ROLE_flight-planner')")
    public List<UUID> simulateSchedules(SimulateSchedulesCommand command) {
        return command.schedules().stream().map(schedule -> {
            UUID messageId = UUID.randomUUID();
            publisher.publishSchedule(messageId, schedule);
            return messageId;
        }).toList();
    }
}
