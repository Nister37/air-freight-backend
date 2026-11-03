package be.kdg.airfreight.flightoperations.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(schema = "flightoperations", name = "fo_flight")
public class FlightJpaEntity {
    @Id private String id;
    @Version private Long version;
    @Column(nullable = false) private String flightNumber;
    @Column(nullable = false) private LocalDate scheduledDepartureDate;
    @Column(nullable = false) private String departureAirport;
    @Column(nullable = false) private String arrivalAirport;
    @Column(nullable = false) private String aircraftRegistration;
    @Column(nullable = false) private LocalDateTime scheduledDeparture;
    @Column(nullable = false) private LocalDateTime scheduledArrival;
    private LocalDateTime actualDeparture;
    private LocalDateTime actualArrival;
    private boolean finalLoadReceived;
    @OneToMany
    @JoinColumn(name = "flight_id")
    private List<DelayJpaEntity> delays = new ArrayList<>();

    public FlightJpaEntity() { }

    public String getFlightNumber() { return flightNumber; }
    public LocalDate getScheduledDepartureDate() { return scheduledDepartureDate; }
    public String getDepartureAirport() { return departureAirport; }
    public String getArrivalAirport() { return arrivalAirport; }
    public String getAircraftRegistration() { return aircraftRegistration; }
    public LocalDateTime getScheduledDeparture() { return scheduledDeparture; }
    public LocalDateTime getScheduledArrival() { return scheduledArrival; }
    public LocalDateTime getActualDeparture() { return actualDeparture; }
    public LocalDateTime getActualArrival() { return actualArrival; }
    public boolean isFinalLoadReceived() { return finalLoadReceived; }
    public List<DelayJpaEntity> getDelays() { return List.copyOf(delays); }

    public void setId(String id) { this.id = id; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public void setScheduledDepartureDate(LocalDate date) { this.scheduledDepartureDate = date; }
    public void setDepartureAirport(String airport) { this.departureAirport = airport; }
    public void setArrivalAirport(String airport) { this.arrivalAirport = airport; }
    public void setAircraftRegistration(String registration) { this.aircraftRegistration = registration; }
    public void setScheduledDeparture(LocalDateTime time) { this.scheduledDeparture = time; }
    public void setScheduledArrival(LocalDateTime time) { this.scheduledArrival = time; }
    public void setActualDeparture(LocalDateTime time) { this.actualDeparture = time; }
    public void setActualArrival(LocalDateTime time) { this.actualArrival = time; }
    public void setFinalLoadReceived(boolean received) { this.finalLoadReceived = received; }
    public void setDelays(List<DelayJpaEntity> delays) {
        this.delays.clear();
        this.delays.addAll(delays);
    }
}
