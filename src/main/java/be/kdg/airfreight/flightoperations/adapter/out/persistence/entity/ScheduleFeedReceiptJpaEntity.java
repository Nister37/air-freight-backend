package be.kdg.airfreight.flightoperations.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "fo_feed_receipt")
public class ScheduleFeedReceiptJpaEntity {
    @Id UUID messageId;
    protected ScheduleFeedReceiptJpaEntity() { }
    public ScheduleFeedReceiptJpaEntity(UUID messageId) { this.messageId = messageId; }
}
