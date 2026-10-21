package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.FlightSchedule;
import be.kdg.airfreight.flightoperations.domain.Flight;
import be.kdg.airfreight.flightoperations.port.out.FlightLoadPort;
import be.kdg.airfreight.flightoperations.port.out.FlightUpdatePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component
class FlightAnnouncement {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightAnnouncement.class);
    private final FlightLoadPort loadPort;
    private final List<FlightUpdatePort> updatePorts;
    private final FlightPolicyReader policyReader;

    FlightAnnouncement(FlightLoadPort loadPort, List<FlightUpdatePort> updatePorts, FlightPolicyReader policyReader) {
        this.loadPort = loadPort;
        this.updatePorts = List.copyOf(updatePorts);
        this.policyReader = policyReader;
    }

    List<FlightFacts> announce(FlightSchedule schedule) {
        List<FlightFacts> created = new ArrayList<>();
        int retentionDays = policyReader.current().flightRetentionDays();
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
            updatePorts.forEach(port -> port.update(flight));
            created.add(flight.facts());
        }
        return List.copyOf(created);
    }
}
