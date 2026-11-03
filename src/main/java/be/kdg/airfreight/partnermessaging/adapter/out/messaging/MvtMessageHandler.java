package be.kdg.airfreight.partnermessaging.adapter.out.messaging;

import be.kdg.airfreight.partnermessaging.domain.enums.MessageKind;

import java.time.LocalDateTime;

public interface MvtMessageHandler {
    String code();
    MessageKind kind();
    LocalDateTime parse(String body, LocalDateTime readAt);
    String format(LocalDateTime movementTime);
}
