package be.kdg.airfreight.loadplanning.adapter.out.persistence.repository;

import be.kdg.airfreight.loadplanning.adapter.out.persistence.entity.LoadPolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadPolicyJpaRepository extends JpaRepository<LoadPolicyJpaEntity, Integer> {
}
