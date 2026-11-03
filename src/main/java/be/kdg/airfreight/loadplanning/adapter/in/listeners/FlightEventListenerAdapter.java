package be.kdg.airfreight.loadplanning.adapter.in.listeners;


import be.kdg.airfreight.common.events.FlightCreatedEvent;
import be.kdg.airfreight.common.events.FlightRemovedEvent;
import be.kdg.airfreight.common.events.FlightTimesChangedEvent;
import be.kdg.airfreight.loadplanning.port.in.usecase.RefreshLoadFlightUseCase;
import be.kdg.airfreight.loadplanning.port.in.usecase.RemoveLoadForFlightUseCase;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class FlightEventListenerAdapter {

    private final RefreshLoadFlightUseCase refreshLoadFlight;
    private final RemoveLoadForFlightUseCase removeLoadForFlight;

    FlightEventListenerAdapter(RefreshLoadFlightUseCase refreshLoadFlight,
                               RemoveLoadForFlightUseCase removeLoadForFlight) {
        this.refreshLoadFlight = refreshLoadFlight;
        this.removeLoadForFlight = removeLoadForFlight;
    }

    @ApplicationModuleListener
    void created(FlightCreatedEvent event) {
        refreshLoadFlight.refreshLoadFlight(event.flightId());
    }

    @ApplicationModuleListener
    void timesChanged(FlightTimesChangedEvent event) {
        refreshLoadFlight.refreshLoadFlight(event.flightId());
    }

    @ApplicationModuleListener
    void removed(FlightRemovedEvent event) {
        removeLoadForFlight.removeLoadForFlight(event.flightId());
    }
}
