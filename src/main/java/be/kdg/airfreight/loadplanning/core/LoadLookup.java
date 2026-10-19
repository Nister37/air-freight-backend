package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.loadplanning.LoadNotFoundException;
import be.kdg.airfreight.loadplanning.domain.Load;
import be.kdg.airfreight.loadplanning.port.out.LoadLoadPort;
import org.springframework.stereotype.Component;

@Component
class LoadLookup {
    private final LoadLoadPort loadPort;

    LoadLookup(LoadLoadPort loadPort) {
        this.loadPort = loadPort;
    }

    Load required(FlightId id) {
        return loadPort.load(id)
                .orElseThrow(() -> new LoadNotFoundException("No load for flight " + id.key()));
    }
}
