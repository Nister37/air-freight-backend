package be.kdg.airfreight.loadplanning.adapter.out.events;

import be.kdg.airfreight.common.events.DomainEvent;
import be.kdg.airfreight.loadplanning.port.out.LoadEventPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class SpringLoadEventPublisherAdapter implements LoadEventPort {

    private final ApplicationEventPublisher publisher;

    SpringLoadEventPublisherAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}
