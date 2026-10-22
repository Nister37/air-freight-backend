package be.kdg.airfreight.partnermessaging.adapter.out.messaging;

import be.kdg.airfreight.partnermessaging.port.out.MessageFormatPort;
import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.PlacedUld;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import java.time.LocalDateTime;
import java.util.*;

@Component
class PartnerMessageFormatAdapter implements MessageFormatPort {
    private final JsonMapper mapper;
    private final MvtTelegramFormatter telegrams;

    PartnerMessageFormatAdapter(JsonMapper mapper, MvtTelegramFormatter telegrams) {
        this.mapper = mapper;
        this.telegrams = telegrams;
    }
    @Override
    public String flightList(UUID messageId, LocalDateTime producedAt, int version, FlightFacts flight) {
        return json(messageId, producedAt, version, FlmMessage.from(flight));
    }

    @Override
    public String cargoPositions(UUID messageId, LocalDateTime producedAt, int version, FlightId flight,
                                 List<PlacedUld> placedUlds) {
        return json(messageId, producedAt, version, CpmMessage.from(flight, placedUlds));
    }

    @Override
    public String telegram(FlightFacts flight, MessageKind kind, LocalDateTime movementTime) {
        return telegrams.toText(flight, kind, movementTime);
    }

    private String json(UUID messageId, LocalDateTime producedAt, int version, Object data) {
        return mapper.writeValueAsString(Map.of("messageId", messageId, "producedAt", producedAt,
                "formatVersion", version, "data", data));
    }
}
