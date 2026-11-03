package be.kdg.airfreight.loadplanning.port.out;

import be.kdg.airfreight.loadplanning.domain.vo.LoadPolicy;

public interface LoadPolicyUpdatePort {
    void update(LoadPolicy policy);
}
