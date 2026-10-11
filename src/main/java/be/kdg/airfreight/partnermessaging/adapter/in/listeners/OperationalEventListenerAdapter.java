package be.kdg.airfreight.partnermessaging.adapter.in.listeners;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.common.events.FlightCreatedEvent;
import be.kdg.airfreight.common.events.FlightDepartedEvent;
import be.kdg.airfreight.common.events.FlightLandedEvent;
import be.kdg.airfreight.common.events.FlightRemovedEvent;
import be.kdg.airfreight.common.events.FlightTimesChangedEvent;
import be.kdg.airfreight.common.events.LoadFinalisedEvent;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateArrivalMovementMessageUseCase;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateCargoPositionMessageUseCase;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateDepartureMovementMessageUseCase;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateEstimatedMovementMessagesUseCase;
import be.kdg.airfreight.partnermessaging.port.in.usecase.CreateFlightListMessageUseCase;
import be.kdg.airfreight.partnermessaging.port.in.usecase.RemoveFlightMessagesUseCase;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class OperationalEventListenerAdapter {
    private final CreateFlightListMessageUseCase flightList;
    private final CreateEstimatedMovementMessagesUseCase estimatedMovement;
    private final CreateDepartureMovementMessageUseCase departureMovement;
    private final CreateArrivalMovementMessageUseCase arrivalMovement;
    private final CreateCargoPositionMessageUseCase cargoPosition;
    private final RemoveFlightMessagesUseCase removeMessages;

    OperationalEventListenerAdapter(
            CreateFlightListMessageUseCase flightList,
            CreateEstimatedMovementMessagesUseCase estimatedMovement,
            CreateDepartureMovementMessageUseCase departureMovement,
            CreateArrivalMovementMessageUseCase arrivalMovement,
            CreateCargoPositionMessageUseCase cargoPosition,
            RemoveFlightMessagesUseCase removeMessages) {
        this.flightList = flightList;
        this.estimatedMovement = estimatedMovement;
        this.departureMovement = departureMovement;
        this.arrivalMovement = arrivalMovement;
        this.cargoPosition = cargoPosition;
        this.removeMessages = removeMessages;
    }
    @ApplicationModuleListener void created(FlightCreatedEvent event) { flightList.createFlightListMessage(event); }
    @ApplicationModuleListener void estimates(FlightTimesChangedEvent event) { estimatedMovement.createEstimatedMovementMessages(event); }
    @ApplicationModuleListener void departed(FlightDepartedEvent event) { departureMovement.createDepartureMovementMessage(event); }
    @ApplicationModuleListener void landed(FlightLandedEvent event) { arrivalMovement.createArrivalMovementMessage(event); }
    @ApplicationModuleListener void finalised(LoadFinalisedEvent event) { cargoPosition.createCargoPositionMessage(event); }
    @ApplicationModuleListener void removed(FlightRemovedEvent event) { removeMessages.removeFlightMessages(event); }
}
