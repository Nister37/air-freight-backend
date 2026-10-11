package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.events.FlightRemovedEvent;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;
import be.kdg.airfreight.partnermessaging.port.in.usecase.RemoveFlightMessagesUseCase;
import be.kdg.airfreight.partnermessaging.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
@Transactional
class DefaultRemoveFlightMessagesUseCase implements RemoveFlightMessagesUseCase {
    private final MessageStorePort store;

    DefaultRemoveFlightMessagesUseCase(MessageStorePort store) {
        this.store = store;
    }

    @Override
    public void removeFlightMessages(FlightRemovedEvent event) {
        store.removeForFlight(event.flightId());
    }
}
