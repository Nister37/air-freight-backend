package be.kdg.airfreight.simulation.adapter.in.rest;

import be.kdg.airfreight.simulation.port.in.command.*;
import be.kdg.airfreight.simulation.port.in.usecase.*;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/simulation")
@Profile("simulation & !prod & !production")
class SimulationController {
    private final SimulateSchedulesUseCase schedules;
    private final SimulateCargoBookingsUseCase cargo;

    SimulationController(SimulateSchedulesUseCase schedules, SimulateCargoBookingsUseCase cargo) {
        this.schedules = schedules;
        this.cargo = cargo;
    }

    @PostMapping("/schedules")
    @ResponseStatus(HttpStatus.ACCEPTED)
    List<UUID> schedules(@RequestBody List<ScheduleRequest> requests) {
        requireRecords(requests);
        // Map the complete batch before publishing so invalid input never publishes a partial batch.
        var command = new SimulateSchedulesCommand(requests.stream().map(ScheduleRequest::schedule).toList());
        return schedules.simulateSchedules(command);
    }

    @PostMapping("/cargo-bookings")
    @ResponseStatus(HttpStatus.ACCEPTED)
    List<UUID> cargo(@RequestBody List<CargoRequest> requests) {
        requireRecords(requests);
        var command = new SimulateCargoBookingsCommand(requests.stream().map(CargoRequest::booking).toList());
        return cargo.simulateCargoBookings(command);
    }

    private void requireRecords(List<?> requests) {
        if (requests.isEmpty() || requests.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("At least one non-null input record is required");
        }
    }

}
