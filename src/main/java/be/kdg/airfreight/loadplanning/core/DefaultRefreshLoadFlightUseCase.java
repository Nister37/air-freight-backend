package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.loadplanning.port.out.FlightInfo;
import be.kdg.airfreight.loadplanning.domain.vo.LoadPolicy;
import be.kdg.airfreight.loadplanning.domain.aggregate.Load;
import be.kdg.airfreight.loadplanning.domain.vo.LoadId;
import be.kdg.airfreight.loadplanning.domain.vo.LoadingWindow;
import be.kdg.airfreight.loadplanning.port.in.usecase.RefreshLoadFlightUseCase;
import be.kdg.airfreight.loadplanning.port.out.LoadLoadPort;
import be.kdg.airfreight.loadplanning.port.out.LoadUpdatePort;
import be.kdg.airfreight.loadplanning.port.out.FlightRequestPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
class DefaultRefreshLoadFlightUseCase implements RefreshLoadFlightUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRefreshLoadFlightUseCase.class);

    private final LoadLoadPort loadPort;
    private final List<LoadUpdatePort> updatePorts;
    private final LoadPolicyReader policyReader;
    private final FlightRequestPort flights;

    DefaultRefreshLoadFlightUseCase(LoadLoadPort loadPort, List<LoadUpdatePort> updatePorts,
                                   LoadPolicyReader policyReader, FlightRequestPort flights) {
        this.loadPort = loadPort;
        this.updatePorts = List.copyOf(updatePorts);
        this.policyReader = policyReader;
        this.flights = flights;
    }

    @Override
    public void refreshLoadFlight(FlightId flightId) {
        var current = flights.findFlight(flightId);
        if (current.isEmpty()) {
            LOGGER.info("Ignoring late flight update for removed flight {}", flightId.key());
            return;
        }
        // A retried event can be older than a more recent delay change.
        FlightInfo latest = current.get();
        LoadingWindow window = loadingWindowFor(latest);
        Load load = loadPort.load(latest.id()).orElseGet(() -> Load.open(LoadId.create(), latest.id(),
                latest.aircraftRegistration(), latest.destinationAirport(), window));
        load.updateFlight(latest.destinationAirport(), latest.aircraftRegistration(), window);

        updatePorts.forEach(port -> port.update(load));
    }

    private LoadingWindow loadingWindowFor(FlightInfo facts) {
        LoadPolicy policy = policyReader.current();
        return LoadingWindow.beforeDeparture(facts.scheduledDeparture(), facts.estimatedDeparture(),
                policy.loadingWindowOpeningMinutes(), policy.loadingWindowClosingMinutes());
    }

}


