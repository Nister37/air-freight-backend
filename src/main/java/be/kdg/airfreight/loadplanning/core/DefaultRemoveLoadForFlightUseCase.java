package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.loadplanning.port.in.usecase.RemoveLoadForFlightUseCase;
import be.kdg.airfreight.loadplanning.port.out.LoadLoadPort;
import be.kdg.airfreight.loadplanning.port.out.LoadUpdatePort;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
class DefaultRemoveLoadForFlightUseCase implements RemoveLoadForFlightUseCase {

    private final LoadLoadPort loadPort;
    private final List<LoadUpdatePort> updatePorts;

    DefaultRemoveLoadForFlightUseCase(LoadLoadPort loadPort, List<LoadUpdatePort> updatePorts) {
        this.loadPort = loadPort;
        this.updatePorts = List.copyOf(updatePorts);
    }

    @Override
    public void removeLoadForFlight(FlightId id){
        loadPort.load(id).ifPresent(load -> updatePorts.forEach(port -> port.remove(load)));
    }
}
