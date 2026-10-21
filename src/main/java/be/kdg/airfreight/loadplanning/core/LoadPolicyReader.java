package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.loadplanning.domain.LoadPolicy;
import be.kdg.airfreight.loadplanning.port.out.LoadPolicyLoadPort;
import org.springframework.stereotype.Component;

@Component
class LoadPolicyReader {
    private final LoadPolicyLoadPort loadPort;

    LoadPolicyReader(LoadPolicyLoadPort loadPort) {
        this.loadPort = loadPort;
    }

    LoadPolicy current() {
        return loadPort.load().orElseGet(LoadPolicy::defaults);
    }
}
