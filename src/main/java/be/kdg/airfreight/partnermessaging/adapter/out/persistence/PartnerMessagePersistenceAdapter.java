package be.kdg.airfreight.partnermessaging.adapter.out.persistence;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.events.PartnerMessageCreatedEvent;
import be.kdg.airfreight.partnermessaging.adapter.out.persistence.entity.PartnerMessageJpaEntity;
import be.kdg.airfreight.partnermessaging.adapter.out.persistence.repository.PartnerMessageJpaRepository;
import be.kdg.airfreight.partnermessaging.port.out.MessageStorePort;
import org.slf4j.*;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
class PartnerMessagePersistenceAdapter implements MessageStorePort {
    private static final Logger LOGGER = LoggerFactory.getLogger(PartnerMessagePersistenceAdapter.class);
    private final PartnerMessageJpaRepository repository;
    PartnerMessagePersistenceAdapter(PartnerMessageJpaRepository repository) { this.repository = repository; }
    public boolean saveIfAbsent(UUID sourceEventId, FlightId flightId, PartnerMessageCreatedEvent event) {
        Objects.requireNonNull(sourceEventId);
        Objects.requireNonNull(flightId);
        Objects.requireNonNull(event);
        if (repository.existsBySourceEventIdAndKind(sourceEventId, event.kind())) {
            LOGGER.info("Duplicate partner message ignored: event {}, kind {}", sourceEventId, event.kind());
            return false;
        }
        repository.saveAndFlush(new PartnerMessageJpaEntity(sourceEventId, flightId, event));
        return true;
    }
    public void removeForFlight(FlightId id) { repository.deleteByFlightKey(id.key()); }
}
