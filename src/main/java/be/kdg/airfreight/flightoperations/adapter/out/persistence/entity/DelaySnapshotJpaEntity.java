package be.kdg.airfreight.flightoperations.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "flightoperations", name = "fo_delay_snapshot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"flight_id", "sequence_number"}))
public class DelaySnapshotJpaEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID snapshotId;
    @Column(nullable = false, updatable = false)
    private String flightId;
    @Column(nullable = false, updatable = false)
    private long sequenceNumber;
    @Column(nullable = false, updatable = false)
    private LocalDateTime snapshotDate;
    @Column(nullable = false, columnDefinition = "text", updatable = false)
    private String departureDelays;
    @Column(nullable = false, columnDefinition = "text", updatable = false)
    private String arrivalDelays;

    protected DelaySnapshotJpaEntity() { }

    public DelaySnapshotJpaEntity(UUID snapshotId, String flightId, long sequenceNumber,
                                  LocalDateTime snapshotDate, String departureDelays, String arrivalDelays) {
        this.snapshotId = snapshotId;
        this.flightId = flightId;
        this.sequenceNumber = sequenceNumber;
        this.snapshotDate = snapshotDate;
        this.departureDelays = departureDelays;
        this.arrivalDelays = arrivalDelays;
    }

    public long getSequenceNumber() { return sequenceNumber; }
    public String getDepartureDelays() { return departureDelays; }
    public String getArrivalDelays() { return arrivalDelays; }
}
