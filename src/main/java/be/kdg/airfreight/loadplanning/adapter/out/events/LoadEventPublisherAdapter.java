package be.kdg.airfreight.loadplanning.adapter.out.events;

import be.kdg.airfreight.loadplanning.domain.Load;
import be.kdg.airfreight.loadplanning.port.out.LoadUpdatePort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
class LoadEventPublisherAdapter implements LoadUpdatePort {

    private final ApplicationEventPublisher publisher;

    LoadEventPublisherAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void update(Load load) {
        load.getDomainEvents().forEach(publisher::publishEvent);
        load.clearDomainEvents();
    }

    @Override
    public void remove(Load load) {
        update(load);
    }
}
