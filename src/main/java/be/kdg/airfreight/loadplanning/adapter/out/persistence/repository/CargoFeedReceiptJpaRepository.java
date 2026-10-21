package be.kdg.airfreight.loadplanning.adapter.out.persistence.repository;

import be.kdg.airfreight.loadplanning.adapter.out.persistence.entity.CargoFeedReceiptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CargoFeedReceiptJpaRepository extends JpaRepository<CargoFeedReceiptJpaEntity, UUID> { }
