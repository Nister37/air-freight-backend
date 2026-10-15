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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
class DefaultReceiveFlightFactsUseCase implements ReceiveFlightFactsUseCase {

    private final LoadLoadPort loadPort;
    private final LoadUpdatePort updatePort;
    private final PolicyQuery policyQuery;

    DefaultReceiveFlightFactsUseCase(LoadLoadPort loadPort, LoadUpdatePort updatePort, PolicyQuery policyQuery) {
        this.loadPort = loadPort;
        this.updatePort = updatePort;
        this.policyQuery = policyQuery;
    }

    @Override
    public void receiveFlightFacts(FlightFacts facts) {
        LoadingWindow window = loadingWindowFor(facts);

        Load load = loadPort.load(facts.id())
                .map(existing -> {
                    existing.updateFlightFacts(facts.arrivalAirport(), facts.aircraftRegistration(), window);
                    return existing;
                })
                .orElseGet(() -> Load.open(LoadId.create(), facts.id(),
                        facts.aircraftRegistration(), facts.arrivalAirport(), window));

        updatePort.update(load);
    }

    private LoadingWindow loadingWindowFor(FlightFacts facts) {
        OperationsPolicy policy = policyQuery.currentPolicy();
        return LoadingWindow.beforeDeparture(facts.scheduledDeparture(),
                policy.loadingWindowOpeningMinutes(), policy.loadingWindowClosingMinutes());
    }

}


