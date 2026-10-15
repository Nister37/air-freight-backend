package be.kdg.airfreight.loadplanning.adapter.out.persistence.repository;

import be.kdg.airfreight.loadplanning.adapter.out.persistence.entity.AircraftJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AircraftJpaRepository extends JpaRepository<AircraftJpaEntity, String> {
}