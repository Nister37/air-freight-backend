package be.kdg.airfreight.loadplanning.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "lp_feed_receipt")
public class CargoFeedReceiptJpaEntity {
    @Id UUID messageId;
    protected CargoFeedReceiptJpaEntity() { }
    public CargoFeedReceiptJpaEntity(UUID messageId) { this.messageId = messageId; }
}
