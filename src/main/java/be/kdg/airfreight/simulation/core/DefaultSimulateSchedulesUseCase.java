package be.kdg.airfreight.simulation.core;

import be.kdg.airfreight.simulation.port.in.command.SimulateSchedulesCommand;
import be.kdg.airfreight.simulation.port.in.usecase.SimulateSchedulesUseCase;
import be.kdg.airfreight.simulation.port.out.SimulationFeedPort;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Profile("simulation & !prod & !production")
class DefaultSimulateSchedulesUseCase implements SimulateSchedulesUseCase {
    private final SimulationFeedPort feeds;

    DefaultSimulateSchedulesUseCase(SimulationFeedPort feeds) { this.feeds = feeds; }

    @Override
    @PreAuthorize("hasAuthority('ROLE_flight-planner')")
    public List<UUID> simulateSchedules(SimulateSchedulesCommand command) {
        return command.schedules().stream().map(schedule -> {
            UUID messageId = UUID.randomUUID();
            feeds.publishSchedule(messageId, schedule);
            return messageId;
        }).toList();
    }
}
