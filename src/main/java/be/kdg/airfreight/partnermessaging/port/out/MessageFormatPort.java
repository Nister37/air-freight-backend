package be.kdg.airfreight.partnermessaging.port.out;

import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.PlacedUld;
import be.kdg.airfreight.partnermessaging.domain.enums.MessageKind;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageFormatPort {
    String flightList(UUID messageId, LocalDateTime producedAt, int version, FlightFacts flight);
    String cargoPositions(UUID messageId, LocalDateTime producedAt, int version, FlightId flight,
                          List<PlacedUld> placedUlds);
    String telegram(FlightFacts flight, MessageKind kind, LocalDateTime movementTime);
}
