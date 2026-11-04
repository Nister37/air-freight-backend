package be.kdg.airfreight.partnermessaging.port.out;

import be.kdg.airfreight.common.events.PartnerMessageCreatedEvent;

public interface PartnerMessageEventPort {
    void publish(PartnerMessageCreatedEvent event);
}
