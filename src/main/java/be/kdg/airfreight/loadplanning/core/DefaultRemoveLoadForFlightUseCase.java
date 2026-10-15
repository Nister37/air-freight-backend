package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.loadplanning.port.in.usecase.RemoveLoadForFlightUseCase;
import be.kdg.airfreight.loadplanning.port.out.LoadLoadPort;
import be.kdg.airfreight.loadplanning.port.out.LoadUpdatePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
class DefaultRemoveLoadForFlightUseCase implements RemoveLoadForFlightUseCase {

    private final LoadLoadPort loadPort;
    private final LoadUpdatePort updatePort;

    DefaultRemoveLoadForFlightUseCase(LoadLoadPort loadPort, LoadUpdatePort updatePort) {
        this.loadPort = loadPort;
        this.updatePort = updatePort;
    }

    @Override
    public void removeLoadForFlight(FlightId id){
        loadPort.load(id).ifPresent(updatePort::remove);
    }
}
