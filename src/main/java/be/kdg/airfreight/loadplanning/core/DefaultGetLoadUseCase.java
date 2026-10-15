package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.loadplanning.LoadNotFoundException;
import be.kdg.airfreight.loadplanning.port.in.usecase.GetLoadUseCase;
import be.kdg.airfreight.loadplanning.port.out.LoadLoadPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
class DefaultGetLoadUseCase implements GetLoadUseCase {

    private final LoadLoadPort loadPort;

    DefaultGetLoadUseCase(LoadLoadPort loadPort) {
        this.loadPort = loadPort;
    }

    @Override
    public LoadFacts getLoad(FlightId id){
        return loadPort.load(id)
                .orElseThrow(() -> new LoadNotFoundException("No load for flight "+ id.key()))
                .facts();
    }

}
