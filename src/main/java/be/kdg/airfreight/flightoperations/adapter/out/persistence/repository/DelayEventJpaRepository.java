package be.kdg.airfreight.flightoperations.adapter.out.persistence.repository;

import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.DelayEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DelayEventJpaRepository extends JpaRepository<DelayEventJpaEntity, UUID> {
    Optional<DelayEventJpaEntity> findFirstByFlightIdOrderBySequenceNumberDesc(String flightId);
    List<DelayEventJpaEntity> findByFlightIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(
            String flightId, long sequenceNumber);
    void deleteByFlightId(String flightId);
}
