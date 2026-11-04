package be.kdg.airfreight.partnermessaging.port.out;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.events.PartnerMessageCreatedEvent;

import java.util.UUID;

public interface MessageStorePort {
    boolean saveIfAbsent(UUID sourceEventId, FlightId flightId, PartnerMessageCreatedEvent event);
    void removeForFlight(FlightId id);
}
