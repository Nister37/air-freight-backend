package be.kdg.airfreight.partnermessaging.adapter.out.persistence;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.partnermessaging.adapter.out.persistence.entity.PartnerMessageJpaEntity;
import be.kdg.airfreight.partnermessaging.adapter.out.persistence.repository.PartnerMessageJpaRepository;
import be.kdg.airfreight.partnermessaging.domain.PartnerMessage;
import be.kdg.airfreight.partnermessaging.port.out.MessageStorePort;
import org.slf4j.*;
import org.springframework.stereotype.Component;

@Component
class PartnerMessagePersistenceAdapter implements MessageStorePort {
    private static final Logger LOGGER = LoggerFactory.getLogger(PartnerMessagePersistenceAdapter.class);
    private final PartnerMessageJpaRepository repository;
    PartnerMessagePersistenceAdapter(PartnerMessageJpaRepository repository) { this.repository = repository; }
    public boolean saveIfAbsent(PartnerMessage message) {
        if (repository.existsBySourceEventIdAndKind(message.sourceEventId(), message.kind())) {
            LOGGER.info("Duplicate partner message ignored: event {}, kind {}", message.sourceEventId(), message.kind());
            return false;
        }
        repository.saveAndFlush(new PartnerMessageJpaEntity(message));
        return true;
    }
    public void removeForFlight(FlightId id) { repository.deleteByFlightKey(id.key()); }
}
