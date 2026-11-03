package be.kdg.airfreight.partnermessaging.adapter.out.persistence.entity;

import be.kdg.airfreight.partnermessaging.domain.aggregate.PartnerMessage;

import be.kdg.airfreight.partnermessaging.domain.enums.MessageKind;

import be.kdg.airfreight.common.*;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(schema = "partnermessaging", name = "pm_message")
public class PartnerMessageJpaEntity {
    @Id UUID messageId;
    @Column(nullable = false) UUID sourceEventId;
    @Column(nullable = false) LocalDateTime producedAt;
    int formatVersion;
    @Column(nullable = false) String flightKey;
    String flightNumber;
    LocalDate scheduledDepartureDate;
    String departureAirport;
    @Enumerated(EnumType.STRING) MessageKind kind;
    @Column(nullable = false, columnDefinition = "text") String body;
    protected PartnerMessageJpaEntity() { }
    public PartnerMessageJpaEntity(PartnerMessage message) {
        messageId = message.id(); sourceEventId = message.sourceEventId();
        producedAt = message.producedAt(); formatVersion = message.formatVersion();
        flightKey = message.flightId().key(); flightNumber = message.flightId().flightNumber().value();
        scheduledDepartureDate = message.flightId().scheduledDepartureDate();
        departureAirport = message.flightId().departureAirport().value(); kind = message.kind(); body = message.body();
    }
}
