package be.kdg.airfreight.loadplanning.adapter.out.persistence.repository;

import be.kdg.airfreight.loadplanning.adapter.out.persistence.entity.LoadJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoadJpaRepository extends JpaRepository<LoadJpaEntity, UUID> {
    Optional<LoadJpaEntity> findByFlightId(String flightId);
}