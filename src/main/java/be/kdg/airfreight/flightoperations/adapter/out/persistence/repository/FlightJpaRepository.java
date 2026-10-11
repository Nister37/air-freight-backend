package be.kdg.airfreight.flightoperations.adapter.out.persistence.repository;

import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.FlightJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;

public interface FlightJpaRepository extends JpaRepository<FlightJpaEntity, String> {
    List<FlightJpaEntity> findByScheduledDepartureDateLessThanEqual(LocalDate date);
}
