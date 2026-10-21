package be.kdg.airfreight.partnermessaging.adapter.out.messaging;

import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;

import java.time.LocalDateTime;

public interface MvtMessageHandler {
    String code();
    MessageKind kind();
    Airport airport(FlightFacts flight);
    LocalDateTime parse(String body, LocalDateTime readAt);
    String format(LocalDateTime movementTime);
}
