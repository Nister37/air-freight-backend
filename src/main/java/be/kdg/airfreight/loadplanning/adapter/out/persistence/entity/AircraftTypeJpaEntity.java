package be.kdg.airfreight.loadplanning.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lp_aircraft_type")
public class AircraftTypeJpaEntity {

    @Id
    private String typeCode;

    @Column(nullable = false)
    private String rowSlots;     // "C,D,E,F,G,H,J,K,L,M,P,R,S,T,U"

    @Column(nullable = false)
    private String noseSlots;    // "A1,A2,B1"

    @Column(nullable = false)
    private String tailSlots;    // "X"

    @Column(nullable = false)
    private String doorRow;      // "R"

    protected AircraftTypeJpaEntity() { }

    public String getRowSlots() { return rowSlots; }
    public String getNoseSlots() { return noseSlots; }
    public String getTailSlots() { return tailSlots; }
    public String getDoorRow() { return doorRow; }
}
