package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.loadplanning.LoadNotFoundException;
import be.kdg.airfreight.loadplanning.domain.FloorPlan;
import be.kdg.airfreight.loadplanning.domain.Load;
import be.kdg.airfreight.loadplanning.domain.Position;
import be.kdg.airfreight.loadplanning.domain.ULDCode;
import be.kdg.airfreight.loadplanning.port.in.command.PlaceULDCommand;
import be.kdg.airfreight.loadplanning.port.in.usecase.PlaceULDUseCase;
import be.kdg.airfreight.loadplanning.port.out.AircraftLoadPort;
import be.kdg.airfreight.loadplanning.port.out.LoadEventPort;
import be.kdg.airfreight.loadplanning.port.out.LoadLoadPort;
import be.kdg.airfreight.loadplanning.port.out.LoadUpdatePort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Transactional
class DefaultPlaceULDUseCase implements PlaceULDUseCase {

    private final LoadLoadPort loadPort;
    private final LoadUpdatePort updatePort;
    private final LoadEventPort eventPort;
    private final AircraftLoadPort aircraftPort;

    DefaultPlaceULDUseCase(LoadLoadPort loadPort, LoadUpdatePort updatePort,
                           LoadEventPort eventPort, AircraftLoadPort aircraftPort) {
        this.loadPort = loadPort;
        this.updatePort = updatePort;
        this.eventPort = eventPort;
        this.aircraftPort = aircraftPort;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_load-planner')")
    public LoadFacts placeULD(PlaceULDCommand command) {
        Load load = required(command.flightId());
        FloorPlan floorPlan = floorPlanFor(load);

        Position position  =floorPlan.find(command.position())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Position " + command.position() + " does not exist on this aircraft"));

        load.place(new ULDCode(command.uldCode()), position, floorPlan, LocalDateTime.now(ZoneOffset.UTC));
        return save(load);
    }


    private Load required(FlightId id) {
        return loadPort.load(id)
                .orElseThrow(() -> new LoadNotFoundException("No load for flight " + id.key()));
    }

    private FloorPlan floorPlanFor(Load load) {
        return aircraftPort.floorPlanFor(load.aircraftRegistration())
                .orElseThrow(() -> new IllegalStateException(
                        "No floor plan known for aircraft " + load.aircraftRegistration().value()));
    }

    private LoadFacts save(Load load) {
        updatePort.update(load);
        load.getDomainEvents().forEach(eventPort::publish);
        load.clearDomainEvents();
        return load.facts();
    }
}
