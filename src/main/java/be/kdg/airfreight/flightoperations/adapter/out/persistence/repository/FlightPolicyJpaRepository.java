package be.kdg.airfreight.flightoperations.adapter.out.persistence.repository;

import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.FlightPolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightPolicyJpaRepository extends JpaRepository<FlightPolicyJpaEntity, Integer> {
}
