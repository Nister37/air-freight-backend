package be.kdg.airfreight.partnermessaging.port.out;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.messages.PlacedUld;
import be.kdg.airfreight.common.messages.MessageKind;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageFormatPort {
    String flightList(UUID messageId, LocalDateTime producedAt, int version, FlightId flightId,
                      Airport arrivalAirport, LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival);
    String cargoPositions(UUID messageId, LocalDateTime producedAt, int version, FlightId flight,
                          List<PlacedUld> placedUlds);
    String telegram(FlightId flightId, AircraftRegistration aircraftRegistration, Airport airport,
                    MessageKind kind, LocalDateTime movementTime);
}
