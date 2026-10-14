package be.kdg.airfreight.loadplanning.port.out;

import be.kdg.airfreight.common.events.DomainEvent;

public interface LoadEventPort {
    void publish(DomainEvent event);
}
