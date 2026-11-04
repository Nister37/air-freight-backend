package be.kdg.airfreight.flightoperations.adapter.out.persistence.repository;

import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.DelaySnapshotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DelaySnapshotJpaRepository extends JpaRepository<DelaySnapshotJpaEntity, UUID> {
    Optional<DelaySnapshotJpaEntity> findFirstByFlightIdOrderBySequenceNumberDesc(String flightId);
    void deleteByFlightId(String flightId);
}
