package be.kdg.airfreight.operationscoordination.adapter.out.persistence.repository;

import be.kdg.airfreight.operationscoordination.adapter.out.persistence.entity.PolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyJpaRepository extends JpaRepository<PolicyJpaEntity, Integer> { }
