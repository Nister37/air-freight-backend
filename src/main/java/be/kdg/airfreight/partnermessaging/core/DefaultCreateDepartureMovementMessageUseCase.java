package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.events.FlightDepartedEvent;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateDepartureMovementMessageUseCase;
import be.kdg.airfreight.partnermessaging.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
@Transactional
class DefaultCreateDepartureMovementMessageUseCase implements CreateDepartureMovementMessageUseCase {
    private final PartnerMessageCreation messages;

    DefaultCreateDepartureMovementMessageUseCase(PartnerMessageCreation messages) {
        this.messages = messages;
    }

    @Override
    public void createDepartureMovementMessage(FlightDepartedEvent event) {
        messages.createTelegram(event, event.flight(), MessageKind.MVT_AD, event.flight().actualDeparture());
    }
}
