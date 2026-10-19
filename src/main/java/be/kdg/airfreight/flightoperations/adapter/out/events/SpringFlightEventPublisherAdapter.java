package be.kdg.airfreight.flightoperations.adapter.out.events;

import be.kdg.airfreight.flightoperations.domain.Flight;
import be.kdg.airfreight.flightoperations.port.out.FlightUpdatePort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
class SpringFlightEventPublisherAdapter implements FlightUpdatePort {
    private final ApplicationEventPublisher publisher;
    SpringFlightEventPublisherAdapter(ApplicationEventPublisher publisher) { this.publisher = publisher; }
    @Override
    public void update(Flight flight) {
        flight.getDomainEvents().forEach(publisher::publishEvent);
        flight.clearDomainEvents();
    }

    @Override
    public void remove(Flight flight) {
        update(flight);
    }
}
