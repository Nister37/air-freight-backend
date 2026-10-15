package be.kdg.airfreight.loadplanning.adapter.out.persistence.repository;

import be.kdg.airfreight.loadplanning.adapter.out.persistence.entity.UldJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UldJpaRepository extends JpaRepository<UldJpaEntity, Long> {
    List<UldJpaEntity> findByLoadId(UUID loadId);
}
