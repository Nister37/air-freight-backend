package be.kdg.airfreight.loadplanning.adapter.out.persistence.entity;

import be.kdg.airfreight.loadplanning.domain.enums.Side;
import be.kdg.airfreight.loadplanning.domain.enums.WeightUnit;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(schema = "loadplanning", name = "lp_uld")
public class UldJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID loadId;

    @Column(nullable = false)
    private String uldCode;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal weightAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeightUnit weightUnit;

    @Column(nullable = false)
    private String destinationAirport;

    @Enumerated(EnumType.STRING)
    private Side positionSide;      // null = booked, not placed

    private String positionSlot;    // null = booked, not placed

    public UldJpaEntity() { }

    public void setLoadId(UUID loadId) { this.loadId = loadId; }
    public String getUldCode() { return uldCode; }
    public void setUldCode(String uldCode) { this.uldCode = uldCode; }
    public BigDecimal getWeightAmount() { return weightAmount; }
    public void setWeightAmount(BigDecimal weightAmount) { this.weightAmount = weightAmount; }
    public WeightUnit getWeightUnit() { return weightUnit; }
    public void setWeightUnit(WeightUnit weightUnit) { this.weightUnit = weightUnit; }
    public String getDestinationAirport() { return destinationAirport; }
    public void setDestinationAirport(String destinationAirport) { this.destinationAirport = destinationAirport; }
    public Side getPositionSide() { return positionSide; }
    public void setPositionSide(Side positionSide) { this.positionSide = positionSide; }
    public String getPositionSlot() { return positionSlot; }
    public void setPositionSlot(String positionSlot) { this.positionSlot = positionSlot; }
}
