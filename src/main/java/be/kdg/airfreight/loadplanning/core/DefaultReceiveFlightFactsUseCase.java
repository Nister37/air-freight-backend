package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.common.OperationsPolicy;
import be.kdg.airfreight.common.PolicyQuery;
import be.kdg.airfreight.loadplanning.domain.Load;
import be.kdg.airfreight.loadplanning.domain.LoadId;
import be.kdg.airfreight.loadplanning.domain.LoadingWindow;
import be.kdg.airfreight.loadplanning.port.in.usecase.ReceiveFlightFactsUseCase;
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
class DefaultReceiveFlightFactsUseCase implements ReceiveFlightFactsUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultReceiveFlightFactsUseCase.class);

    private final LoadLoadPort loadPort;
    private final List<LoadUpdatePort> updatePorts;
    private final PolicyQuery policyQuery;
    private final FlightRequestPort flights;

    DefaultReceiveFlightFactsUseCase(LoadLoadPort loadPort, List<LoadUpdatePort> updatePorts,
                                   PolicyQuery policyQuery, FlightRequestPort flights) {
        this.loadPort = loadPort;
        this.updatePorts = List.copyOf(updatePorts);
        this.policyQuery = policyQuery;
        this.flights = flights;
    }

    @Override
    public void receiveFlightFacts(FlightFacts facts) {
        var current = flights.findFlightFacts(facts.id());
        if (current.isEmpty()) {
            LOGGER.info("Ignoring late flight facts for removed flight {}", facts.id().key());
            return;
        }
        // A retried event can be older than a more recent delay change.
        FlightFacts latest = current.get();
        LoadingWindow window = loadingWindowFor(latest);
        Load load = loadPort.load(latest.id()).orElseGet(() -> Load.open(LoadId.create(), latest.id(),
                latest.aircraftRegistration(), latest.arrivalAirport(), window));
        load.updateFlightFacts(latest.arrivalAirport(), latest.aircraftRegistration(), window);

        updatePorts.forEach(port -> port.update(load));
    }

    private LoadingWindow loadingWindowFor(FlightFacts facts) {
        OperationsPolicy policy = policyQuery.currentPolicy();
        return LoadingWindow.beforeDeparture(facts.scheduledDeparture(), facts.estimatedDeparture(),
                policy.loadingWindowOpeningMinutes(), policy.loadingWindowClosingMinutes());
    }

}


