package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.events.FlightTimesChangedEvent;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateEstimatedMovementMessagesUseCase;
import be.kdg.airfreight.partnermessaging.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
@Transactional
class DefaultCreateEstimatedMovementMessagesUseCase implements CreateEstimatedMovementMessagesUseCase {
    private final PartnerMessageCreation messages;

    DefaultCreateEstimatedMovementMessagesUseCase(PartnerMessageCreation messages) {
        this.messages = messages;
    }

    @Override
    public void createEstimatedMovementMessages(FlightTimesChangedEvent event) {
        if (event.departureChanged()) messages.createTelegram(event, event.flight(), MessageKind.MVT_ED, event.flight().estimatedDeparture());
        if (event.arrivalChanged()) messages.createTelegram(event, event.flight(), MessageKind.MVT_EA, event.flight().estimatedArrival());
    }
}
