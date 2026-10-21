package be.kdg.airfreight.flightoperations.adapter.out.persistence.repository;

import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.ScheduleFeedReceiptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ScheduleFeedReceiptJpaRepository extends JpaRepository<ScheduleFeedReceiptJpaEntity, UUID> { }
