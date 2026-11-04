package be.kdg.airfreight.partnermessaging.adapter.out.persistence.entity;

import be.kdg.airfreight.common.events.PartnerMessageCreatedEvent;

import be.kdg.airfreight.common.messages.MessageKind;

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
    public PartnerMessageJpaEntity(UUID sourceEventId, FlightId flightId, PartnerMessageCreatedEvent event) {
        messageId = event.eventUUID(); this.sourceEventId = sourceEventId;
        producedAt = event.eventPit(); formatVersion = event.formatVersion();
        flightKey = flightId.key(); flightNumber = flightId.flightNumber().value();
        scheduledDepartureDate = flightId.scheduledDepartureDate();
        departureAirport = flightId.departureAirport().value(); kind = event.kind(); body = event.body();
    }
}
