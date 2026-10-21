package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.common.FlightSchedule;
import be.kdg.airfreight.flightoperations.port.in.usecase.AnnounceFlightsUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
class DefaultAnnounceFlightsUseCase implements AnnounceFlightsUseCase {
    private final FlightAnnouncement flightAnnouncement;

    DefaultAnnounceFlightsUseCase(FlightAnnouncement flightAnnouncement) {
        this.flightAnnouncement = flightAnnouncement;
    }

    @Override
    public List<FlightFacts> announceFlights(FlightSchedule schedule) {
        return flightAnnouncement.announce(schedule);
    }
}
