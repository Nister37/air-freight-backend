package be.kdg.airfreight.operationscoordination.adapter.out.persistence;

import be.kdg.airfreight.operationscoordination.adapter.out.persistence.entity.FeedReceiptJpaEntity;
import be.kdg.airfreight.operationscoordination.adapter.out.persistence.repository.FeedReceiptJpaRepository;
import be.kdg.airfreight.operationscoordination.port.out.FeedReceiptPort;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
class FeedReceiptPersistenceAdapter implements FeedReceiptPort {
    private final FeedReceiptJpaRepository repository;
    FeedReceiptPersistenceAdapter(FeedReceiptJpaRepository repository) {
        this.repository = repository;
    }
    public boolean contains(UUID messageId) { return repository.existsById(messageId); }
    public void record(UUID messageId) {
        repository.saveAndFlush(new FeedReceiptJpaEntity(messageId));
    }
}
