package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.events.FlightLandedEvent;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateArrivalMovementMessageUseCase;
import be.kdg.airfreight.partnermessaging.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
@Transactional
class DefaultCreateArrivalMovementMessageUseCase implements CreateArrivalMovementMessageUseCase {
    private final PartnerMessageCreation messages;

    DefaultCreateArrivalMovementMessageUseCase(PartnerMessageCreation messages) {
        this.messages = messages;
    }

    @Override
    public void createArrivalMovementMessage(FlightLandedEvent event) {
        messages.createTelegram(event, event.flight(), MessageKind.MVT_AA, event.flight().actualArrival());
    }
}
