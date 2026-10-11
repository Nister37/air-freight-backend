package be.kdg.airfreight.partnermessaging.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.events.FlightCreatedEvent;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateFlightListMessageUseCase;
import be.kdg.airfreight.partnermessaging.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
@Transactional
class DefaultCreateFlightListMessageUseCase implements CreateFlightListMessageUseCase {
    private final PartnerMessageCreation messages;

    DefaultCreateFlightListMessageUseCase(PartnerMessageCreation messages) {
        this.messages = messages;
    }

    @Override
    public void createFlightListMessage(FlightCreatedEvent event) {
        FlightFacts f = event.flight();
        Map<String, Object> data = Map.of("flightNumber", f.id().flightNumber().value(),
                "scheduledDepartureDate", f.id().scheduledDepartureDate(), "departureAirport", f.id().departureAirport().value(),
                "arrivalAirport", f.arrivalAirport().value(), "STD", f.scheduledDeparture(), "STA", f.scheduledArrival());
        messages.createJson(event, f.id(), MessageKind.FLM, data);
    }
}
