package be.kdg.airfreight.flightoperations.adapter.out.persistence.repository;

import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.FlightJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.*;

public interface FlightJpaRepository extends JpaRepository<FlightJpaEntity, String> {
    List<FlightJpaEntity> findByScheduledDepartureDateLessThanEqual(LocalDate date);

    @Query("""
            select flight from FlightJpaEntity flight
            where (:date is null or flight.scheduledDepartureDate = :date)
              and (:number is null or flight.flightNumber = :number)
              and (:departureAirport is null or flight.departureAirport = :departureAirport)
              and (:arrivalAirport is null or flight.arrivalAirport = :arrivalAirport)
              and (:status is null
                or (:status = 'SCHEDULED' and flight.actualDeparture is null and flight.actualArrival is null)
                or (:status = 'DEPARTED' and flight.actualDeparture is not null and flight.actualArrival is null)
                or (:status = 'LANDED' and flight.actualArrival is not null))
            order by flight.scheduledDeparture
            """)
    List<FlightJpaEntity> findFlights(LocalDate date, String number, String departureAirport,
                                    String arrivalAirport, String status);
}
