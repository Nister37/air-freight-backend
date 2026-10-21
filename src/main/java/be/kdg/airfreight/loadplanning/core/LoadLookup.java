package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.loadplanning.domain.LoadingWindow;
import be.kdg.airfreight.loadplanning.port.out.FlightRequestPort;
import be.kdg.airfreight.loadplanning.LoadNotFoundException;
import be.kdg.airfreight.loadplanning.domain.Load;
import be.kdg.airfreight.loadplanning.port.out.LoadLoadPort;
import org.springframework.stereotype.Component;

@Component
class LoadLookup {
    private final LoadLoadPort loadPort;
    private final FlightRequestPort flights;
    private final LoadPolicyReader policyReader;

    LoadLookup(LoadLoadPort loadPort, FlightRequestPort flights, LoadPolicyReader policyReader) {
        this.loadPort = loadPort;
        this.flights = flights;
        this.policyReader = policyReader;
    }

    Load required(FlightId id) {
        Load load = loadPort.load(id)
                .orElseThrow(() -> new LoadNotFoundException("No load for flight " + id.key()));
        var facts = flights.findFlightFacts(id)
                .orElseThrow(() -> new LoadNotFoundException("Flight no longer exists: " + id.key()));
        var policy = policyReader.current();
        load.updateFlightFacts(facts.arrivalAirport(), facts.aircraftRegistration(),
                LoadingWindow.beforeDeparture(facts.scheduledDeparture(), facts.estimatedDeparture(),
                        policy.loadingWindowOpeningMinutes(), policy.loadingWindowClosingMinutes()));
        return load;
    }
}
