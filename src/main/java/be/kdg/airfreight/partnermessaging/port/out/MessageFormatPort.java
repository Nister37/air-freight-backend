package be.kdg.airfreight.partnermessaging.port.out;

import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;
import java.time.LocalDateTime;
import java.util.UUID;

public interface MessageFormatPort {
    String json(UUID messageId, LocalDateTime producedAt, int version, Object data);
    String telegram(FlightFacts flight, MessageKind kind, LocalDateTime movementTime);
}
