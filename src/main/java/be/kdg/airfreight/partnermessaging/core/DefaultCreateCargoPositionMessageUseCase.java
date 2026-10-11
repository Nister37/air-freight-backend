package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.events.LoadFinalisedEvent;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateCargoPositionMessageUseCase;
import be.kdg.airfreight.partnermessaging.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
@Transactional
class DefaultCreateCargoPositionMessageUseCase implements CreateCargoPositionMessageUseCase {
    private final PartnerMessageCreation messages;
    private final FinalLoadPort finalLoadPort;

    DefaultCreateCargoPositionMessageUseCase(PartnerMessageCreation messages, FinalLoadPort finalLoadPort) {
        this.messages = messages;
        this.finalLoadPort = finalLoadPort;
    }

    @Override
    public void createCargoPositionMessage(LoadFinalisedEvent event) {
        Map<String, Object> data = Map.of("flightNumber", event.flightId().flightNumber().value(),
                "scheduledDepartureDate", event.flightId().scheduledDepartureDate(),
                "departureAirport", event.flightId().departureAirport().value(), "placedUlds", event.placedUlds());
        if (messages.createJson(event, event.flightId(), MessageKind.CPM, data)) {
            finalLoadPort.processCPM(event.flightId());
        }
    }
}
