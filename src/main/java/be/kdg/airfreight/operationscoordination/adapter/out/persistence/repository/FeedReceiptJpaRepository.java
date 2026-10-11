package be.kdg.airfreight.operationscoordination.adapter.out.persistence.repository;

import be.kdg.airfreight.operationscoordination.adapter.out.persistence.entity.FeedReceiptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FeedReceiptJpaRepository extends JpaRepository<FeedReceiptJpaEntity, UUID> { }
