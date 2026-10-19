package be.kdg.airfreight.partnermessaging.adapter.out.messaging;

import be.kdg.airfreight.partnermessaging.port.out.MessageFormatPort;
import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import java.time.LocalDateTime;
import java.util.*;

@Component
class PartnerMessageFormatAdapter implements MessageFormatPort {
    private final JsonMapper mapper;
    PartnerMessageFormatAdapter(JsonMapper mapper) { this.mapper = mapper; }
    public String json(UUID messageId, LocalDateTime producedAt, int version, Object data) {
        return mapper.writeValueAsString(Map.of("messageId", messageId, "producedAt", producedAt,
                "formatVersion", version, "data", data));
    }
    public String telegram(FlightFacts flight, MessageKind kind, LocalDateTime movementTime) {
        boolean departure = kind == MessageKind.MVT_ED || kind == MessageKind.MVT_AD;
        return new MvtTelegram(flight.id().flightNumber(), flight.id().scheduledDepartureDate(), flight.aircraftRegistration(),
                departure ? flight.id().departureAirport() : flight.arrivalAirport(), kind, movementTime).toText();
    }
}
