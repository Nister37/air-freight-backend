package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.partnermessaging.domain.enums.MessageKind;

import be.kdg.airfreight.partnermessaging.events.PartnerMessageCreatedEvent;
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
    private static final int FORMAT_VERSION = 1;
    private final MessageStorePort store;
    private final MessageFormatPort formatter;
    private final PartnerMessageEventPort events;
    private final FinalLoadPort finalLoadPort;

    PartnerMessageCreation(MessageStorePort store, MessageFormatPort formatter,
                     PartnerMessageEventPort events, FinalLoadPort finalLoadPort) {
        this.store = store; this.formatter = formatter; this.events = events; this.finalLoadPort = finalLoadPort;
    }

    void createFlightList(UUID sourceEventId, LocalDateTime producedAt, FlightFacts flight) {
        if (!retained(flight.id())) return;
        UUID id = UUID.randomUUID();
        saveAndPublish(new PartnerMessage(id, sourceEventId, producedAt, FORMAT_VERSION, flight.id(), MessageKind.FLM,
                formatter.flightList(id, producedAt, FORMAT_VERSION, flight)));
    }

    boolean createCargoPositions(UUID sourceEventId, LocalDateTime producedAt, FlightId flightId,
                                 List<PlacedUld> placedUlds) {
        if (!retained(flightId)) return false;
        UUID id = UUID.randomUUID();
        saveAndPublish(new PartnerMessage(id, sourceEventId, producedAt, FORMAT_VERSION, flightId, MessageKind.CPM,
                formatter.cargoPositions(id, producedAt, FORMAT_VERSION, flightId, placedUlds)));
        return true;
    }

    void createTelegram(UUID sourceEventId, LocalDateTime producedAt, FlightFacts flight, MessageKind kind,
                        LocalDateTime movement) {
        if (!retained(flight.id())) return;
        saveAndPublish(new PartnerMessage(UUID.randomUUID(), sourceEventId, producedAt, FORMAT_VERSION,
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
