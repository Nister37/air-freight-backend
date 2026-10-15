package be.kdg.airfreight.loadplanning.adapter.out.persistence.entity;

import be.kdg.airfreight.loadplanning.domain.LoadStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lp_load")
public class LoadJpaEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true)
    private String flightId;

    @Column(nullable = false)
    private String flightNumber;

    @Column(nullable = false)
    private LocalDate scheduledDepartureDate;

    @Column(nullable = false)
    private String departureAirport;

    @Column(nullable = false)
    private String aircraftRegistration;

    @Column(nullable = false)
    private String destinationAirport;

    @Column(nullable = false)
    private LocalDateTime loadingWindowOpensAt;

    @Column(nullable = false)
    private LocalDateTime loadingWindowClosesAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoadStatus status;

    public LoadJpaEntity() { }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Long getVersion() { return version; }
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public LocalDate getScheduledDepartureDate() { return scheduledDepartureDate; }
    public void setScheduledDepartureDate(LocalDate scheduledDepartureDate) { this.scheduledDepartureDate = scheduledDepartureDate; }
    public String getDepartureAirport() { return departureAirport; }
    public void setDepartureAirport(String departureAirport) { this.departureAirport = departureAirport; }
    public String getAircraftRegistration() { return aircraftRegistration; }
    public void setAircraftRegistration(String aircraftRegistration) { this.aircraftRegistration = aircraftRegistration; }
    public String getDestinationAirport() { return destinationAirport; }
    public void setDestinationAirport(String destinationAirport) { this.destinationAirport = destinationAirport; }
    public LocalDateTime getLoadingWindowOpensAt() { return loadingWindowOpensAt; }
    public void setLoadingWindowOpensAt(LocalDateTime loadingWindowOpensAt) { this.loadingWindowOpensAt = loadingWindowOpensAt; }
    public LocalDateTime getLoadingWindowClosesAt() { return loadingWindowClosesAt; }
    public void setLoadingWindowClosesAt(LocalDateTime loadingWindowClosesAt) { this.loadingWindowClosesAt = loadingWindowClosesAt; }
    public LoadStatus getStatus() { return status; }
    public void setStatus(LoadStatus status) { this.status = status; }
}