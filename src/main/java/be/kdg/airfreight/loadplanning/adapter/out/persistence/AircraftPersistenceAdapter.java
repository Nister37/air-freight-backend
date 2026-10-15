package be.kdg.airfreight.loadplanning.adapter.out.persistence;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.loadplanning.adapter.out.persistence.entity.AircraftTypeJpaEntity;
import be.kdg.airfreight.loadplanning.adapter.out.persistence.repository.AircraftJpaRepository;
import be.kdg.airfreight.loadplanning.adapter.out.persistence.repository.AircraftTypeJpaRepository;
import be.kdg.airfreight.loadplanning.domain.FloorPlan;
import be.kdg.airfreight.loadplanning.domain.Slot;
import be.kdg.airfreight.loadplanning.port.out.AircraftLoadPort;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
class AircraftPersistenceAdapter implements AircraftLoadPort {

    private final AircraftJpaRepository aircraft;
    private final AircraftTypeJpaRepository aircraftTypes;

    AircraftPersistenceAdapter(AircraftJpaRepository aircraft, AircraftTypeJpaRepository aircraftTypes) {
        this.aircraft = aircraft;
        this.aircraftTypes = aircraftTypes;
    }

    @Override
    public Optional<FloorPlan> floorPlanFor(AircraftRegistration registration) {
        return aircraft.findById(registration.value())
                .flatMap(row -> aircraftTypes.findById(row.getTypeCode()))
                .map(AircraftPersistenceAdapter::toFloorPlan);
    }

    private static FloorPlan toFloorPlan(AircraftTypeJpaEntity type) {
        return new FloorPlan(
                slots(type.getRowSlots()),
                slots(type.getNoseSlots()),
                slots(type.getTailSlots()),
                new Slot(type.getDoorRow()));
    }

    private static List<Slot> slots(String commaSeparated) {
        return Arrays.stream(commaSeparated.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(Slot::new)
                .toList();
    }
}