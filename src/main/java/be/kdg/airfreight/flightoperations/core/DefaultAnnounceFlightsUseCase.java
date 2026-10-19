package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.domain.*;
import be.kdg.airfreight.flightoperations.port.in.usecase.AnnounceFlightsUseCase;
import be.kdg.airfreight.flightoperations.port.out.*;
import java.time.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultAnnounceFlightsUseCase implements AnnounceFlightsUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAnnounceFlightsUseCase.class);
    private final FlightLoadPort loadPort;
    private final List<FlightUpdatePort> updatePorts;
    private final PolicyQuery policyQuery;

    DefaultAnnounceFlightsUseCase(FlightLoadPort loadPort, List<FlightUpdatePort> updatePorts, PolicyQuery policyQuery) {
        this.loadPort = loadPort;
        this.updatePorts = List.copyOf(updatePorts);
        this.policyQuery = policyQuery;
    }

    @Override
    public List<FlightFacts> announceFlights(FlightSchedule schedule) {
        List<FlightFacts> created = new ArrayList<>();
        int retentionDays = policyQuery.currentPolicy().flightRetentionDays();
        for (LocalDate date : schedule.operatingDates()) {
            FlightId id = new FlightId(schedule.flightNumber(), date, schedule.departureAirport());
            if (!date.plusDays(retentionDays).isAfter(LocalDate.now(ZoneOffset.UTC))) {
                LOGGER.info("Schedule flight already outside retention ignored: {}", id.key());
                continue;
            }
            if (loadPort.load(id).isPresent()) {
                LOGGER.info("Duplicate schedule flight ignored: {}", id.key());
                continue;
            }
            Flight flight = Flight.announce(schedule, date);
            save(flight);
            created.add(flight.facts());
        }
        return List.copyOf(created);
    }

    private FlightFacts save(Flight flight) {
        updatePorts.forEach(port -> port.update(flight));
        return flight.facts();
    }
}
