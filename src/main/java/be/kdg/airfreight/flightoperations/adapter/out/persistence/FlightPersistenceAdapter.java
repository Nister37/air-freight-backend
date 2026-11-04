package be.kdg.airfreight.flightoperations.adapter.out.persistence;

import be.kdg.airfreight.flightoperations.domain.entity.Arrival;
import be.kdg.airfreight.flightoperations.domain.entity.Delay;
import be.kdg.airfreight.flightoperations.domain.entity.Departure;
import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.flightoperations.domain.vo.DelayCode;
import be.kdg.airfreight.flightoperations.domain.vo.DelayId;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.domain.enums.DelayPhase;
import be.kdg.airfreight.common.events.DelayEvent;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.DelayJpaEntity;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.DelayEventJpaEntity;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.DelaySnapshotJpaEntity;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.FlightJpaEntity;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.repository.DelayEventJpaRepository;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.repository.DelaySnapshotJpaRepository;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.repository.DelayJpaRepository;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.repository.FlightJpaRepository;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightSelection;
import be.kdg.airfreight.flightoperations.port.out.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Component
@Order(1)
class FlightPersistenceAdapter implements FlightLoadPort, FlightUpdatePort {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightPersistenceAdapter.class);
    private static final int DELAY_SNAPSHOT_INTERVAL = 10;
    private final FlightJpaRepository repository;
    private final DelayJpaRepository delays;
    private final DelayEventJpaRepository delayEvents;
    private final DelaySnapshotJpaRepository delaySnapshots;
    private final JsonMapper mapper;
    private final EntityManager entityManager;

    FlightPersistenceAdapter(FlightJpaRepository repository, DelayJpaRepository delays,
                             DelayEventJpaRepository delayEvents, DelaySnapshotJpaRepository delaySnapshots,
                             JsonMapper mapper, EntityManager entityManager) {
        this.repository = repository;
        this.delays = delays;
        this.delayEvents = delayEvents;
        this.delaySnapshots = delaySnapshots;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }
    public Optional<Flight> load(FlightId id) { return repository.findById(id.key()).map(this::toDomain); }
    public List<Flight> findFlights(FlightSelection selection) {
        return repository.findFlights(selection.date(),
                selection.number() == null ? null : selection.number().value(),
                selection.departureAirport() == null ? null : selection.departureAirport().value(),
                selection.arrivalAirport() == null ? null : selection.arrivalAirport().value(),
                selection.status() == null ? null : selection.status().name())
                .stream().map(this::toDomain).toList();
    }
    public List<Flight> loadExpired(LocalDate date) {
        return repository.findByScheduledDepartureDateLessThanEqual(date).stream().map(this::toDomain).toList();
    }
    public void update(Flight flight) {
        String flightKey = flight.getId().key();
        FlightJpaEntity entity = repository.findById(flightKey).orElseGet(FlightJpaEntity::new);
        Optional<DelaySnapshotJpaEntity> snapshot = delaySnapshots.findFirstByFlightIdOrderBySequenceNumberDesc(flightKey);
        // Existing rows are an honest cutover baseline; earlier delay history is not invented.
        List<Delay> baselineDeparture = storedDelays(entity, DelayPhase.DEPARTURE);
        List<Delay> baselineArrival = storedDelays(entity, DelayPhase.ARRIVAL);
        List<DelayEvent> pending = flight.getDomainEvents().stream().filter(DelayEvent.class::isInstance)
                .map(DelayEvent.class::cast).filter(event -> !delayEvents.existsById(event.eventUUID())).toList();
        long sequence = delayEvents.findFirstByFlightIdOrderBySequenceNumberDesc(flightKey)
                .map(DelayEventJpaEntity::getSequenceNumber).orElse(0L);
        if (snapshot.isEmpty() && sequence != 0) throw new IllegalStateException("Delay history has no baseline snapshot");
        List<DelayJpaEntity> previousDelays = new ArrayList<>(entity.getDelays());
        updateEntity(entity, flight);
        delays.saveAll(entity.getDelays());
        for (DelayJpaEntity previous : previousDelays) {
            if (!entity.getDelays().contains(previous)) delays.delete(previous);
        }
        repository.saveAndFlush(entity);
        if (!pending.isEmpty()) {
            // A description-only change must also conflict with concurrent departure/arrival changes.
            entityManager.lock(entity, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        }
        if (snapshot.isEmpty()) saveDelaySnapshot(flightKey, 0, baselineDeparture, baselineArrival);
        List<DelayEventJpaEntity> appended = new ArrayList<>();
        for (DelayEvent event : pending) appended.add(new DelayEventJpaEntity(flightKey, ++sequence, event));
        delayEvents.saveAll(appended);
        long checkpoint = snapshot.map(DelaySnapshotJpaEntity::getSequenceNumber).orElse(0L);
        if (sequence - checkpoint >= DELAY_SNAPSHOT_INTERVAL) {
            saveDelaySnapshot(flightKey, sequence, flight.getDepartureDelays(), flight.getArrivalDelays());
        }
    }
    public void remove(Flight flight) {
        repository.findById(flight.getId().key()).ifPresent(entity -> {
            delayEvents.deleteByFlightId(flight.getId().key());
            delaySnapshots.deleteByFlightId(flight.getId().key());
            delays.deleteAll(entity.getDelays());
            repository.delete(entity);
        });
    }

    private void updateEntity(FlightJpaEntity entity, Flight flight) {
        entity.setId(flight.getId().key());
        entity.setFlightNumber(flight.getId().flightNumber().value());
        entity.setScheduledDepartureDate(flight.getId().scheduledDepartureDate());
        entity.setDepartureAirport(flight.getId().departureAirport().value());
        entity.setArrivalAirport(flight.getArrivalAirport().value());
        entity.setAircraftRegistration(flight.getAircraftRegistration().value());
        entity.setScheduledDeparture(flight.getScheduledDeparture());
        entity.setScheduledArrival(flight.getScheduledArrival());
        entity.setActualDeparture(flight.getActualDeparture());
        entity.setActualArrival(flight.getActualArrival());
        entity.setFinalLoadReceived(flight.isFinalLoadReceived());

        List<DelayJpaEntity> previousDelays = entity.getDelays();
        List<DelayJpaEntity> updatedDelays = new ArrayList<>();
        for (Delay delay : flight.getDepartureDelays()) {
            updatedDelays.add(toEntity(delay, DelayPhase.DEPARTURE, previousDelays));
        }
        for (Delay delay : flight.getArrivalDelays()) {
            updatedDelays.add(toEntity(delay, DelayPhase.ARRIVAL, previousDelays));
        }
        entity.setDelays(updatedDelays);
    }

    private DelayJpaEntity toEntity(Delay delay, DelayPhase phase, List<DelayJpaEntity> previousDelays) {
        DelayJpaEntity entity = previousDelays.stream()
                .filter(previous -> previous.getDelayId().equals(delay.id().value()))
                .findFirst().orElseGet(DelayJpaEntity::new);
        entity.setDelayId(delay.id().value());
        entity.setCode(delay.code().value());
        entity.setDescription(delay.description());
        entity.setMinutes(delay.minutes());
        entity.setPhase(phase);
        return entity;
    }

    private Flight toDomain(FlightJpaEntity entity) {
        String flightKey = new FlightId(new FlightNumber(entity.getFlightNumber()), entity.getScheduledDepartureDate(),
                new Airport(entity.getDepartureAirport())).key();
        Optional<DelaySnapshotJpaEntity> snapshot = delaySnapshots.findFirstByFlightIdOrderBySequenceNumberDesc(flightKey);
        long checkpoint = snapshot.map(DelaySnapshotJpaEntity::getSequenceNumber).orElse(0L);
        List<DelayEventJpaEntity> tail = delayEvents
                .findByFlightIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(flightKey, checkpoint);
        if (snapshot.isEmpty() && !tail.isEmpty()) {
            // The first history transaction may have committed between the two reads.
            snapshot = Optional.of(delaySnapshots.findFirstByFlightIdOrderBySequenceNumberDesc(flightKey)
                    .orElseThrow(() -> new IllegalStateException("Delay history has no baseline snapshot")));
            checkpoint = snapshot.get().getSequenceNumber();
            tail = delayEvents.findByFlightIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(flightKey, checkpoint);
        }
        List<Delay> departureDelays = snapshot.map(row -> snapshotDelays(row.getDepartureDelays()))
                .orElseGet(() -> storedDelays(entity, DelayPhase.DEPARTURE));
        List<Delay> arrivalDelays = snapshot.map(row -> snapshotDelays(row.getArrivalDelays()))
                .orElseGet(() -> storedDelays(entity, DelayPhase.ARRIVAL));
        long expectedSequence = checkpoint;
        for (DelayEventJpaEntity event : tail) {
            if (event.getSequenceNumber() != ++expectedSequence) {
                throw new IllegalStateException("Incomplete delay history for " + flightKey);
            }
        }
        Departure departure = Departure.restore(entity.getScheduledDeparture(), entity.getActualDeparture(),
                departureDelays);
        Arrival arrival = Arrival.restore(entity.getScheduledArrival(), entity.getActualArrival(),
                arrivalDelays);
        LOGGER.debug("Restoring flight {} from delay snapshot {} and {} later events", flightKey, checkpoint, tail.size());
        return Flight.replayDelays(new FlightId(new FlightNumber(entity.getFlightNumber()), entity.getScheduledDepartureDate(),
                        new Airport(entity.getDepartureAirport())), new Airport(entity.getArrivalAirport()),
                new AircraftRegistration(entity.getAircraftRegistration()), departure, arrival, entity.isFinalLoadReceived(),
                tail.stream().map(this::toDomainEvent).toList());
    }

    private List<Delay> storedDelays(FlightJpaEntity entity, DelayPhase phase) {
        return entity.getDelays().stream().filter(delay -> delay.getPhase() == phase).map(this::toDomainDelay).toList();
    }

    private List<Delay> snapshotDelays(String json) {
        return Arrays.asList(mapper.readValue(json, Delay[].class));
    }

    private void saveDelaySnapshot(String flightKey, long sequence, List<Delay> departure, List<Delay> arrival) {
        delaySnapshots.save(new DelaySnapshotJpaEntity(UUID.randomUUID(), flightKey, sequence,
                LocalDateTime.now(ZoneOffset.UTC), mapper.writeValueAsString(departure), mapper.writeValueAsString(arrival)));
        LOGGER.info("Saved delay snapshot for flight {} through event {}", flightKey, sequence);
    }

    private DelayEvent toDomainEvent(DelayEventJpaEntity entity) {
        return new DelayEvent(entity.getEventId(), entity.getEventPit(), entity.getType().name(), entity.getPhase().name(),
                entity.getDelayId(), entity.getCode(), entity.getDescription(), entity.getMinutes());
    }

    private Delay toDomainDelay(DelayJpaEntity entity) {
        return new Delay(new DelayId(entity.getDelayId()), new DelayCode(entity.getCode()),
                entity.getDescription(), entity.getMinutes());
    }
}
