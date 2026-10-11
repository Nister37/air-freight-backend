package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.partnermessaging.events.PartnerMessageCreatedEvent;
import be.kdg.airfreight.common.events.DomainEvent;
import be.kdg.airfreight.common.*;
import be.kdg.airfreight.partnermessaging.domain.*;
import be.kdg.airfreight.partnermessaging.port.out.*;
import org.springframework.stereotype.Component;
import org.slf4j.*;
import java.time.LocalDateTime;
import java.util.*;

@Component
class PartnerMessageCreation {
    private static final Logger LOGGER = LoggerFactory.getLogger(PartnerMessageCreation.class);
    private final MessageStorePort store;
    private final MessageFormatPort formatter;
    private final PartnerMessageEventPort events;
    private final FinalLoadPort finalLoadPort;

    PartnerMessageCreation(MessageStorePort store, MessageFormatPort formatter,
                     PartnerMessageEventPort events, FinalLoadPort finalLoadPort) {
        this.store = store; this.formatter = formatter; this.events = events; this.finalLoadPort = finalLoadPort;
    }

    boolean createJson(DomainEvent event, FlightId flightId, MessageKind kind, Object data) {
        if (!retained(flightId)) return false;
        UUID id = UUID.randomUUID();
        saveAndPublish(new PartnerMessage(id, event.eventUUID(), event.eventPit(), 1, flightId, kind,
                formatter.json(id, event.eventPit(), 1, data)));
        return true;
    }

    void createTelegram(DomainEvent event, FlightFacts flight, MessageKind kind, LocalDateTime movement) {
        if (!retained(flight.id())) return;
        saveAndPublish(new PartnerMessage(UUID.randomUUID(), event.eventUUID(), event.eventPit(), 1,
                flight.id(), kind, formatter.telegram(flight, kind, movement)));
    }

    private void saveAndPublish(PartnerMessage message) {
        if (store.saveIfAbsent(message)) {
            events.publish(new PartnerMessageCreatedEvent(message));
        }
    }

    private boolean retained(FlightId id) {
        if (finalLoadPort.flightExists(id)) return true;
        LOGGER.warn("Late message for missing/removed flight ignored: {}", id.key());
        return false;
    }
}
