package be.kdg.airfreight.flightoperations.adapter.out.persistence;

import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.ScheduleFeedReceiptJpaEntity;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.repository.ScheduleFeedReceiptJpaRepository;
import be.kdg.airfreight.flightoperations.port.out.ScheduleFeedReceiptPort;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
class ScheduleFeedReceiptPersistenceAdapter implements ScheduleFeedReceiptPort {
    private final ScheduleFeedReceiptJpaRepository repository;
    ScheduleFeedReceiptPersistenceAdapter(ScheduleFeedReceiptJpaRepository repository) {
        this.repository = repository;
    }
    public boolean contains(UUID messageId) { return repository.existsById(messageId); }
    public void record(UUID messageId) {
        repository.saveAndFlush(new ScheduleFeedReceiptJpaEntity(messageId));
    }
}
