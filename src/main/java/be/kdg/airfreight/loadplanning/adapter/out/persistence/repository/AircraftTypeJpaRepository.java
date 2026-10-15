package be.kdg.airfreight.loadplanning.adapter.out.persistence.repository;

import be.kdg.airfreight.loadplanning.adapter.out.persistence.entity.AircraftTypeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AircraftTypeJpaRepository extends JpaRepository<AircraftTypeJpaEntity, String> {
}