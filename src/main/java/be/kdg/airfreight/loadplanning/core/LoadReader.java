package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.loadplanning.LoadNotFoundException;
import be.kdg.airfreight.loadplanning.domain.enums.LoadStatus;
import be.kdg.airfreight.loadplanning.domain.LoadView;
import be.kdg.airfreight.loadplanning.domain.vo.LoadingWindow;
import be.kdg.airfreight.loadplanning.port.out.FlightRequestPort;
import be.kdg.airfreight.loadplanning.port.out.LoadLoadPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class LoadReader {
    private final LoadLoadPort loadPort;
    private final FlightRequestPort flights;
    private final LoadPolicyReader policyReader;

    LoadReader(LoadLoadPort loadPort, FlightRequestPort flights, LoadPolicyReader policyReader) {
        this.loadPort = loadPort;
        this.flights = flights;
        this.policyReader = policyReader;
    }

    LoadView required(FlightId id) {
        LoadView stored = loadPort.load(id)
                .orElseThrow(() -> new LoadNotFoundException("No load for flight " + id.key())).view();
        var facts = flights.findFlight(id)
                .orElseThrow(() -> new LoadNotFoundException("Flight no longer exists: " + id.key()));
        var policy = policyReader.current();
        if (stored.status() == LoadStatus.FINAL) return stored;

        LoadingWindow window = LoadingWindow.beforeDeparture(facts.scheduledDeparture(), facts.estimatedDeparture(),
                policy.loadingWindowOpeningMinutes(), policy.loadingWindowClosingMinutes());
        boolean aircraftChanged = !stored.aircraftRegistration().equals(facts.aircraftRegistration());
        boolean factsChanged = aircraftChanged || !stored.destinationAirport().equals(facts.destinationAirport())
                || !stored.loadingWindow().equals(window);
        return new LoadView(stored.flightId(), facts.aircraftRegistration(), facts.destinationAirport(), window,
                stored.status(), stored.ulds(), aircraftChanged ? List.of() : stored.placements(),
                factsChanged ? List.of() : stored.checks());
    }
}
