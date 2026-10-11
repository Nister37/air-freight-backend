package be.kdg.airfreight.flightoperations.adapter.out.events;

import be.kdg.airfreight.common.events.DomainEvent;
import be.kdg.airfreight.flightoperations.port.out.FlightEventPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class SpringFlightEventPublisherAdapter implements FlightEventPort {
    private final ApplicationEventPublisher publisher;
    SpringFlightEventPublisherAdapter(ApplicationEventPublisher publisher) { this.publisher = publisher; }
    public void publish(DomainEvent event) { publisher.publishEvent(event); }
}
