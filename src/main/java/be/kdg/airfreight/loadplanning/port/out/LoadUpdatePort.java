package be.kdg.airfreight.loadplanning.port.out;

import be.kdg.airfreight.loadplanning.domain.Load;

public interface LoadUpdatePort {
    void update(Load load);
    void remove(Load load);
}
