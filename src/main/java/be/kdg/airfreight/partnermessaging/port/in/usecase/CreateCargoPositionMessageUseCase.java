package be.kdg.airfreight.partnermessaging.port.in.usecase;

import be.kdg.airfreight.common.events.LoadFinalisedEvent;

public interface CreateCargoPositionMessageUseCase {
    void createCargoPositionMessage(LoadFinalisedEvent event);
}
