package be.kdg.airfreight.flightoperations.adapter.out.persistence.entity;

import be.kdg.airfreight.flightoperations.domain.enums.DelayEventType;
import be.kdg.airfreight.flightoperations.domain.enums.DelayPhase;
import be.kdg.airfreight.common.events.DelayEvent;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/** One immutable change in a flight's delay history. */
@Entity
@Table(schema = "flightoperations", name = "fo_delay_event",
        uniqueConstraints = @UniqueConstraint(columnNames = {"flight_id", "sequence_number"}))
public class DelayEventJpaEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID eventId;
    @Column(nullable = false, updatable = false)
    private String flightId;
    @Column(nullable = false, updatable = false)
    private long sequenceNumber;
    @Column(nullable = false, updatable = false)
    private LocalDateTime eventPit;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private DelayEventType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private DelayPhase phase;
    @Column(nullable = false, updatable = false)
    private UUID delayId;
    @Column(nullable = false, updatable = false)
    private String code;
    @Column(nullable = false, length = 4000, updatable = false)
    private String description;
    @Column(nullable = false, updatable = false)
    private int minutes;

    protected DelayEventJpaEntity() { }

    public DelayEventJpaEntity(String flightId, long sequenceNumber, DelayEvent event) {
        this.eventId = event.eventUUID();
        this.flightId = flightId;
        this.sequenceNumber = sequenceNumber;
        this.eventPit = event.eventPit();
        this.type = DelayEventType.valueOf(event.type());
        this.phase = DelayPhase.valueOf(event.phase());
        this.delayId = event.delayId();
        this.code = event.code();
        this.description = event.description();
        this.minutes = event.minutes();
    }

    public UUID getEventId() { return eventId; }
    public long getSequenceNumber() { return sequenceNumber; }
    public LocalDateTime getEventPit() { return eventPit; }
    public DelayEventType getType() { return type; }
    public DelayPhase getPhase() { return phase; }
    public UUID getDelayId() { return delayId; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public int getMinutes() { return minutes; }
}
