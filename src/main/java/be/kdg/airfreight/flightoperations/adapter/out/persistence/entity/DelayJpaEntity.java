package be.kdg.airfreight.flightoperations.adapter.out.persistence.entity;

import be.kdg.airfreight.flightoperations.domain.enums.DelayPhase;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = "flightoperations", name = "fo_delay")
public class DelayJpaEntity {
    @Id private UUID delayId;
    private String code;
    @Column(length = 4000) private String description;
    private int minutes;
    @Enumerated(EnumType.STRING) private DelayPhase phase;

    public DelayJpaEntity() { }

    public UUID getDelayId() { return delayId; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public int getMinutes() { return minutes; }
    public DelayPhase getPhase() { return phase; }

    public void setDelayId(UUID delayId) { this.delayId = delayId; }
    public void setCode(String code) { this.code = code; }
    public void setDescription(String description) { this.description = description; }
    public void setMinutes(int minutes) { this.minutes = minutes; }
    public void setPhase(DelayPhase phase) { this.phase = phase; }
}
