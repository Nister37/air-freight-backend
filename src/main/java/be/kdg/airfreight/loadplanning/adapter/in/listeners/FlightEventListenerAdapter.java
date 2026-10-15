package be.kdg.airfreight.loadplanning.adapter.in.listeners;


import be.kdg.airfreight.common.events.FlightCreatedEvent;
import be.kdg.airfreight.common.events.FlightRemovedEvent;
import be.kdg.airfreight.common.events.FlightTimesChangedEvent;
import be.kdg.airfreight.loadplanning.port.in.usecase.ReceiveFlightFactsUseCase;
import be.kdg.airfreight.loadplanning.port.in.usecase.RemoveLoadForFlightUseCase;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class FlightEventListenerAdapter {

    private final ReceiveFlightFactsUseCase receiveFlightFacts;
    private final RemoveLoadForFlightUseCase removeLoadForFlight;

    FlightEventListenerAdapter(ReceiveFlightFactsUseCase receiveFlightFacts,
                               RemoveLoadForFlightUseCase removeLoadForFlight) {
        this.receiveFlightFacts = receiveFlightFacts;
        this.removeLoadForFlight = removeLoadForFlight;
    }

    @ApplicationModuleListener
    void created(FlightCreatedEvent event) {
        receiveFlightFacts.receiveFlightFacts(event.flight());
    }

    @ApplicationModuleListener
    void timesChanged(FlightTimesChangedEvent event) {
        receiveFlightFacts.receiveFlightFacts(event.flight());
    }

    @ApplicationModuleListener
    void removed(FlightRemovedEvent event) {
        removeLoadForFlight.removeLoadForFlight(event.flightId());
    }
}
