package be.kdg.airfreight.partnermessaging.events;

import be.kdg.airfreight.common.events.DomainEvent;
import be.kdg.airfreight.partnermessaging.domain.aggregate.PartnerMessage;
import java.time.LocalDateTime;
import java.util.UUID;

public record PartnerMessageCreatedEvent(PartnerMessage message) implements DomainEvent {
    public UUID eventUUID() { return message.id(); }
    public LocalDateTime eventPit() { return message.producedAt(); }
}
