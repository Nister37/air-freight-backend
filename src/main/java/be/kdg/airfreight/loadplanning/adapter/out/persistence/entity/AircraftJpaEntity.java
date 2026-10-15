package be.kdg.airfreight.loadplanning.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lp_aircraft")
public class AircraftJpaEntity {

    @Id
    private String registration;

    @Column(nullable = false)
    private String typeCode;

    protected AircraftJpaEntity() { }

    public String getRegistration() { return registration; }
    public String getTypeCode() { return typeCode; }
}