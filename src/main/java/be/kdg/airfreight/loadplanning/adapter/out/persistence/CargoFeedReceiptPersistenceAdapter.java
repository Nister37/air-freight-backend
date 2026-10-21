package be.kdg.airfreight.loadplanning.adapter.out.persistence;

import be.kdg.airfreight.loadplanning.adapter.out.persistence.entity.CargoFeedReceiptJpaEntity;
import be.kdg.airfreight.loadplanning.adapter.out.persistence.repository.CargoFeedReceiptJpaRepository;
import be.kdg.airfreight.loadplanning.port.out.CargoFeedReceiptPort;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
class CargoFeedReceiptPersistenceAdapter implements CargoFeedReceiptPort {
    private final CargoFeedReceiptJpaRepository repository;
    CargoFeedReceiptPersistenceAdapter(CargoFeedReceiptJpaRepository repository) {
        this.repository = repository;
    }
    public boolean contains(UUID messageId) { return repository.existsById(messageId); }
    public void record(UUID messageId) {
        repository.saveAndFlush(new CargoFeedReceiptJpaEntity(messageId));
    }
}
