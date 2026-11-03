package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.flightoperations.domain.aggregate.Flight;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.flightoperations.domain.vo.FlightSchedule;
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

    List<Flight> announce(FlightSchedule schedule) {
        List<Flight> created = new ArrayList<>();
        int retentionDays = policyReader.current().flightRetentionDays();
        for (LocalDate date : operatingDates(schedule)) {
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
            created.add(flight);
        }
        return List.copyOf(created);
    }

    private List<LocalDate> operatingDates(FlightSchedule schedule) {
        return schedule.from().datesUntil(schedule.to().plusDays(1))
                .filter(date -> schedule.weekdays().contains(date.getDayOfWeek())).toList();
    }
}
