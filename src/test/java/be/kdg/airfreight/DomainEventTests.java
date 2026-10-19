package be.kdg.airfreight;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.events.*;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;
import be.kdg.airfreight.partnermessaging.domain.PartnerMessage;
import be.kdg.airfreight.partnermessaging.events.PartnerMessageCreatedEvent;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class DomainEventTests {
    private FlightFacts flight() {
        LocalDateTime departure = LocalDateTime.parse("2026-10-11T10:00:00");
        LocalDateTime arrival = departure.plusHours(2);
        return new FlightFacts(new FlightId(new FlightNumber("HJ221"), departure.toLocalDate(), new Airport("BRU")),
                new Airport("ATH"), new AircraftRegistration("SXBAA"), departure, arrival, departure, arrival,
                null, null, FlightStatus.SCHEDULED, false, List.of());
    }

    private List<DomainEvent> newEvents(FlightFacts flight) {
        return List.of(new FlightCreatedEvent(flight), new FlightDepartedEvent(flight),
                new FlightLandedEvent(flight), new FlightRemovedEvent(flight.id()),
                new FlightTimesChangedEvent(flight, true, false), new LoadFinalisedEvent(flight.id(), List.of()));
    }

    @Test
    void newEventsReceiveDistinctIdsAndTheirCreationTimeInUtc() {
        FlightFacts flight = flight();
        LocalDateTime before = LocalDateTime.now(ZoneOffset.UTC);
        List<DomainEvent> events = new ArrayList<>(newEvents(flight));
        events.addAll(newEvents(flight));
        LocalDateTime after = LocalDateTime.now(ZoneOffset.UTC);

        assertThat(events).extracting(DomainEvent::eventUUID).doesNotContainNull().doesNotHaveDuplicates();
        for (DomainEvent event : events) {
            assertThat(event.eventPit()).isBetween(before, after);
        }
    }

    @Test
    void restoringRecordedEventsPreservesTheirOriginalMetadataAndPayload() {
        UUID id = UUID.fromString("21731b72-6d23-4b58-aeba-05393f122341");
        LocalDateTime recordedAt = LocalDateTime.parse("2026-01-09T06:45:00");
        FlightFacts flight = flight();
        PartnerMessage message = new PartnerMessage(id, id, recordedAt, 1, flight.id(), MessageKind.CPM, "{\"data\":[]}");
        List<DomainEvent> events = List.of(
                new FlightCreatedEvent(id, recordedAt, flight),
                new FlightDepartedEvent(id, recordedAt, flight),
                new FlightLandedEvent(id, recordedAt, flight),
                new FlightRemovedEvent(id, recordedAt, flight.id()),
                new FlightTimesChangedEvent(id, recordedAt, flight, true, false),
                new LoadFinalisedEvent(id, recordedAt, flight.id(), List.of()),
                new PartnerMessageCreatedEvent(message));
        JsonMapper mapper = new JsonMapper();

        for (DomainEvent event : events) {
            DomainEvent restored = mapper.readValue(mapper.writeValueAsString(event), event.getClass());
            assertThat(restored).isEqualTo(event);
            assertThat(restored.eventUUID()).isEqualTo(id);
            assertThat(restored.eventPit()).isEqualTo(recordedAt);
        }
    }
}
