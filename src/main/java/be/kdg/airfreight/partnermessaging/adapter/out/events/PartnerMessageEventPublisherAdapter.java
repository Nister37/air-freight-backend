package be.kdg.airfreight.partnermessaging.adapter.out.events;

import be.kdg.airfreight.partnermessaging.events.PartnerMessageCreatedEvent;
import be.kdg.airfreight.partnermessaging.port.out.PartnerMessageEventPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class PartnerMessageEventPublisherAdapter implements PartnerMessageEventPort {
    private final ApplicationEventPublisher publisher;

    PartnerMessageEventPublisherAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(PartnerMessageCreatedEvent event) {
        publisher.publishEvent(event);
    }
}
