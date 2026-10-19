package be.kdg.airfreight.flightoperations.adapter.out.persistence;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.DelayJpaEntity;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.FlightJpaEntity;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.repository.DelayJpaRepository;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.repository.FlightJpaRepository;
import be.kdg.airfreight.flightoperations.domain.*;
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
        FlightJpaEntity entity = repository.findById(flight.facts().id().key()).orElseGet(FlightJpaEntity::new);
        List<DelayJpaEntity> previousDelays = new ArrayList<>(entity.getDelays());
        updateEntity(entity, flight.facts());
        delays.saveAll(entity.getDelays());
        for (DelayJpaEntity previous : previousDelays) {
            if (!entity.getDelays().contains(previous)) delays.delete(previous);
        }
        repository.saveAndFlush(entity);
    }
    public void remove(Flight flight) {
        repository.findById(flight.facts().id().key()).ifPresent(entity -> {
            delays.deleteAll(entity.getDelays());
            repository.delete(entity);
        });
    }

    private void updateEntity(FlightJpaEntity entity, FlightFacts facts) {
        entity.setId(facts.id().key());
        entity.setFlightNumber(facts.id().flightNumber().value());
        entity.setScheduledDepartureDate(facts.id().scheduledDepartureDate());
        entity.setDepartureAirport(facts.id().departureAirport().value());
        entity.setArrivalAirport(facts.arrivalAirport().value());
        entity.setAircraftRegistration(facts.aircraftRegistration().value());
        entity.setScheduledDeparture(facts.scheduledDeparture());
        entity.setScheduledArrival(facts.scheduledArrival());
        entity.setActualDeparture(facts.actualDeparture());
        entity.setActualArrival(facts.actualArrival());
        entity.setFinalLoadReceived(facts.finalLoadReceived());

        List<DelayJpaEntity> previousDelays = entity.getDelays();
        List<DelayJpaEntity> updatedDelays = new ArrayList<>();
        for (DelayDetails details : facts.delays()) {
            DelayJpaEntity delay = previousDelays.stream()
                    .filter(previous -> previous.getDelayId().equals(details.id()))
                    .findFirst().orElseGet(DelayJpaEntity::new);
            delay.setDelayId(details.id());
            delay.setCode(details.code());
            delay.setDescription(details.description());
            delay.setMinutes(details.minutes());
            delay.setPhase(details.phase());
            updatedDelays.add(delay);
        }
        entity.setDelays(updatedDelays);
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
