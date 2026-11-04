package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.common.messages.MessageKind;

import be.kdg.airfreight.common.events.PartnerMessageCreatedEvent;
import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.messages.PlacedUld;
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

    void createFlightList(UUID sourceEventId, LocalDateTime producedAt, FlightId flightId, Airport arrivalAirport,
                          LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) {
        if (!retained(flightId)) return;
        UUID id = UUID.randomUUID();
        saveAndPublish(sourceEventId, flightId, new PartnerMessageCreatedEvent(id, producedAt, FORMAT_VERSION, MessageKind.FLM,
                formatter.flightList(id, producedAt, FORMAT_VERSION, flightId, arrivalAirport, scheduledDeparture, scheduledArrival)));
    }

    boolean createCargoPositions(UUID sourceEventId, LocalDateTime producedAt, FlightId flightId,
                                 List<PlacedUld> placedUlds) {
        if (!retained(flightId)) return false;
        UUID id = UUID.randomUUID();
        saveAndPublish(sourceEventId, flightId, new PartnerMessageCreatedEvent(id, producedAt, FORMAT_VERSION, MessageKind.CPM,
                formatter.cargoPositions(id, producedAt, FORMAT_VERSION, flightId, placedUlds)));
        return true;
    }

    void createTelegram(UUID sourceEventId, LocalDateTime producedAt, FlightId flightId, AircraftRegistration aircraftRegistration,
                        Airport airport, MessageKind kind, LocalDateTime movement) {
        if (!retained(flightId)) return;
        saveAndPublish(sourceEventId, flightId, new PartnerMessageCreatedEvent(UUID.randomUUID(), producedAt, FORMAT_VERSION,
                kind, formatter.telegram(flightId, aircraftRegistration, airport, kind, movement)));
    }

    private void saveAndPublish(UUID sourceEventId, FlightId flightId, PartnerMessageCreatedEvent event) {
        if (store.saveIfAbsent(sourceEventId, flightId, event)) {
            events.publish(event);
        }
    }

    private boolean retained(FlightId id) {
        if (finalLoadPort.flightExists(id)) return true;
        LOGGER.warn("Late message for missing/removed flight ignored: {}", id.key());
        return false;
    }
}
