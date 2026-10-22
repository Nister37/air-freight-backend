package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.partnermessaging.port.in.usecase.RemoveFlightMessagesUseCase;
import be.kdg.airfreight.partnermessaging.port.out.MessageStorePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultRemoveFlightMessagesUseCase implements RemoveFlightMessagesUseCase {
    private final MessageStorePort store;

    DefaultRemoveFlightMessagesUseCase(MessageStorePort store) {
        this.store = store;
    }

    @Override
    public void removeFlightMessages(FlightId flightId) {
        store.removeForFlight(flightId);
    }
}
