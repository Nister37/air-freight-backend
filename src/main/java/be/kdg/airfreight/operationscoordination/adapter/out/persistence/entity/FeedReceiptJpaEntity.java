package be.kdg.airfreight.operationscoordination.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "oc_feed_receipt")
public class FeedReceiptJpaEntity {
    @Id UUID messageId;
    protected FeedReceiptJpaEntity() { }
    public FeedReceiptJpaEntity(UUID messageId) { this.messageId = messageId; }
}
