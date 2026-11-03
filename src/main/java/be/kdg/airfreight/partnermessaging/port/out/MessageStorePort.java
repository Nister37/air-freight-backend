package be.kdg.airfreight.partnermessaging.port.out;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.partnermessaging.domain.aggregate.PartnerMessage;

public interface MessageStorePort {
    boolean saveIfAbsent(PartnerMessage message);
    void removeForFlight(FlightId id);
}
