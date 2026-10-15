package be.kdg.airfreight.loadplanning.adapter.out.persistence;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.FlightNumber;
import be.kdg.airfreight.loadplanning.adapter.out.persistence.entity.LoadCheckJpaEntity;
import be.kdg.airfreight.loadplanning.adapter.out.persistence.entity.LoadJpaEntity;
import be.kdg.airfreight.loadplanning.adapter.out.persistence.entity.UldJpaEntity;
import be.kdg.airfreight.loadplanning.adapter.out.persistence.repository.LoadCheckJpaRepository;
import be.kdg.airfreight.loadplanning.adapter.out.persistence.repository.LoadJpaRepository;
import be.kdg.airfreight.loadplanning.adapter.out.persistence.repository.UldJpaRepository;
import be.kdg.airfreight.loadplanning.domain.*;
import be.kdg.airfreight.loadplanning.port.out.LoadLoadPort;
import be.kdg.airfreight.loadplanning.port.out.LoadUpdatePort;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class LoadPersistenceAdapter implements LoadLoadPort, LoadUpdatePort {

    private final LoadJpaRepository loads;
    private final UldJpaRepository ulds;
    private final LoadCheckJpaRepository checks;

    LoadPersistenceAdapter(LoadJpaRepository loads, UldJpaRepository ulds, LoadCheckJpaRepository checks) {
        this.loads = loads;
        this.ulds = ulds;
        this.checks = checks;
    }

    // ---------- LoadLoadPort ----------

    @Override
    public Optional<Load> load(FlightId id) {
        return loads.findByFlightId(id.key()).map(this::toDomain);
    }

    // ---------- LoadUpdatePort ----------

    @Override
    public void update(Load load) {
        LoadJpaEntity entity = loads.findById(load.loadId().value()).orElseGet(LoadJpaEntity::new);
        updateEntity(entity, load);
        loads.saveAndFlush(entity);
        syncUlds(load);
        replaceChecks(load);
    }

    @Override
    public void remove(Load load) {
        UUID id = load.loadId().value();
        loads.findById(id).ifPresent(entity -> {
            ulds.deleteAll(ulds.findByLoadId(id));
            checks.deleteAll(checks.findByLoadIdOrderByIdAsc(id));
            ulds.flush();
            loads.delete(entity);
        });
    }

    // ---------- Load → rows ----------

    private void updateEntity(LoadJpaEntity entity, Load load) {
        FlightId flight = load.flightId();
        entity.setId(load.loadId().value());
        entity.setFlightId(flight.key());
        entity.setFlightNumber(flight.flightNumber().value());
        entity.setScheduledDepartureDate(flight.scheduledDepartureDate());
        entity.setDepartureAirport(flight.departureAirport().value());
        entity.setAircraftRegistration(load.aircraftRegistration().value());
        entity.setDestinationAirport(load.destinationAirport().value());
        entity.setLoadingWindowOpensAt(load.loadingWindow().opensAt());
        entity.setLoadingWindowClosesAt(load.loadingWindow().closesAt());
        entity.setStatus(load.status());
    }

    // Match existing rows by ULD code: update them, insert new ones, delete ones that disappeared.
    private void syncUlds(Load load) {
        UUID loadId = load.loadId().value();
        Map<String, UldJpaEntity> existing = ulds.findByLoadId(loadId).stream()
                .collect(Collectors.toMap(UldJpaEntity::getUldCode, Function.identity()));
        Map<ULDCode, Position> positions = load.placements().stream()
                .collect(Collectors.toMap(ULDPlacement::code, ULDPlacement::position));

        List<UldJpaEntity> current = new ArrayList<>();
        for (ULD uld : load.ulds()) {
            UldJpaEntity row = existing.remove(uld.code().value());
            if (row == null) {
                row = new UldJpaEntity();
                row.setLoadId(loadId);
                row.setUldCode(uld.code().value());
            }
            row.setWeightAmount(uld.weight().amount());
            row.setWeightUnit(uld.weight().unit());
            row.setDestinationAirport(uld.destination().value());

            Position position = positions.get(uld.code());
            row.setPositionSide(position == null ? null : position.side());
            row.setPositionSlot(position == null ? null : position.slot().value());
            current.add(row);
        }
        ulds.saveAll(current);
        ulds.deleteAll(existing.values());   // whatever is left wasn't in the Load anymore
    }

    // Checks have no identity of their own: the domain rewrites them as a whole, so we do too.
    private void replaceChecks(Load load) {
        UUID loadId = load.loadId().value();
        checks.deleteAll(checks.findByLoadIdOrderByIdAsc(loadId));
        checks.saveAll(load.checks().stream().map(check -> toEntity(loadId, check)).toList());
    }

    private static LoadCheckJpaEntity toEntity(UUID loadId, LoadCheck check) {
        LoadCheckJpaEntity row = new LoadCheckJpaEntity();
        row.setLoadId(loadId);
        row.setKind(check.kind());
        row.setSeverity(check.severity());
        row.setResult(check.result());
        row.setExplanation(check.explanation());
        return row;
    }

    // ---------- rows → Load ----------

    private Load toDomain(LoadJpaEntity entity) {
        List<UldJpaEntity> uldRows = ulds.findByLoadId(entity.getId());

        List<ULD> uldList = uldRows.stream()
                .map(row -> ULD.of(
                        new ULDCode(row.getUldCode()),
                        new Weight(row.getWeightAmount(), row.getWeightUnit()),
                        new Airport(row.getDestinationAirport())))
                .toList();

        List<ULDPlacement> placements = uldRows.stream()
                .filter(row -> row.getPositionSide() != null)
                .map(row -> new ULDPlacement(
                        new ULDCode(row.getUldCode()),
                        new Position(row.getPositionSide(), new Slot(row.getPositionSlot()))))
                .toList();

        List<LoadCheck> checkList = checks.findByLoadIdOrderByIdAsc(entity.getId()).stream()
                .map(row -> new LoadCheck(row.getKind(), row.getSeverity(), row.getResult(), row.getExplanation()))
                .toList();

        return Load.restore(
                new LoadId(entity.getId()),
                new FlightId(new FlightNumber(entity.getFlightNumber()),
                        entity.getScheduledDepartureDate(),
                        new Airport(entity.getDepartureAirport())),
                new AircraftRegistration(entity.getAircraftRegistration()),
                new Airport(entity.getDestinationAirport()),
                new LoadingWindow(entity.getLoadingWindowOpensAt(), entity.getLoadingWindowClosesAt()),
                entity.getStatus(),
                uldList, placements, checkList);
    }


}
