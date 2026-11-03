package be.kdg.airfreight.flightoperations.adapter.out.persistence;

import be.kdg.airfreight.flightoperations.domain.entity.Arrival;
import be.kdg.airfreight.flightoperations.domain.entity.Delay;
import be.kdg.airfreight.flightoperations.domain.entity.Departure;
import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.flightoperations.domain.vo.DelayCode;
import be.kdg.airfreight.flightoperations.domain.vo.DelayId;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.domain.enums.DelayPhase;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.DelayJpaEntity;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.FlightJpaEntity;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.repository.DelayJpaRepository;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.repository.FlightJpaRepository;
import be.kdg.airfreight.flightoperations.port.in.usecase.FlightSelection;
import be.kdg.airfreight.flightoperations.port.out.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.*;

@Component
@Order(1)
class FlightPersistenceAdapter implements FlightLoadPort, FlightUpdatePort {
    private final FlightJpaRepository repository;
    private final DelayJpaRepository delays;
    FlightPersistenceAdapter(FlightJpaRepository repository, DelayJpaRepository delays) {
        this.repository = repository;
        this.delays = delays;
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
        FlightJpaEntity entity = repository.findById(flight.getId().key()).orElseGet(FlightJpaEntity::new);
        List<DelayJpaEntity> previousDelays = new ArrayList<>(entity.getDelays());
        updateEntity(entity, flight);
        delays.saveAll(entity.getDelays());
        for (DelayJpaEntity previous : previousDelays) {
            if (!entity.getDelays().contains(previous)) delays.delete(previous);
        }
        repository.saveAndFlush(entity);
    }
    public void remove(Flight flight) {
        repository.findById(flight.getId().key()).ifPresent(entity -> {
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
        Departure departure = Departure.restore(entity.getScheduledDeparture(), entity.getActualDeparture(),
                entity.getDelays().stream().filter(delay -> delay.getPhase() == DelayPhase.DEPARTURE)
                        .map(this::toDomainDelay).toList());
        Arrival arrival = Arrival.restore(entity.getScheduledArrival(), entity.getActualArrival(),
                entity.getDelays().stream().filter(delay -> delay.getPhase() == DelayPhase.ARRIVAL)
                        .map(this::toDomainDelay).toList());
        return Flight.restore(new FlightId(new FlightNumber(entity.getFlightNumber()), entity.getScheduledDepartureDate(),
                        new Airport(entity.getDepartureAirport())), new Airport(entity.getArrivalAirport()),
                new AircraftRegistration(entity.getAircraftRegistration()), departure, arrival, entity.isFinalLoadReceived());
    }

    private Delay toDomainDelay(DelayJpaEntity entity) {
        return new Delay(new DelayId(entity.getDelayId()), new DelayCode(entity.getCode()),
                entity.getDescription(), entity.getMinutes());
    }
}
