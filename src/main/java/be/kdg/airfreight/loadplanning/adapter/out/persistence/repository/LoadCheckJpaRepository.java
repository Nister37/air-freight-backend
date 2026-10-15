package be.kdg.airfreight.loadplanning.adapter.out.persistence.repository;

import be.kdg.airfreight.loadplanning.adapter.out.persistence.entity.LoadCheckJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoadCheckJpaRepository extends JpaRepository<LoadCheckJpaEntity, Long> {
    List<LoadCheckJpaEntity> findByLoadIdOrderByIdAsc(UUID loadId);
}