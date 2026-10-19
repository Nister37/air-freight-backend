package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.loadplanning.domain.LoadView;
import be.kdg.airfreight.loadplanning.domain.LoadPlan;
import be.kdg.airfreight.loadplanning.port.in.usecase.GetLoadPlanQuery;
import be.kdg.airfreight.loadplanning.port.out.AircraftLoadPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DefaultGetLoadPlanQuery implements GetLoadPlanQuery {
    private final LoadReader loadReader;
    private final AircraftLoadPort aircraftPort;

    DefaultGetLoadPlanQuery(LoadReader loadReader, AircraftLoadPort aircraftPort) {
        this.loadReader = loadReader;
        this.aircraftPort = aircraftPort;
    }

    @Override
    public LoadPlan getLoadPlan(FlightId id) {
        LoadView load = loadReader.required(id);
        return new LoadPlan(load, aircraftPort.floorPlanFor(load.aircraftRegistration())
                .orElseThrow(() -> new IllegalStateException("No floor plan known for aircraft " + load.aircraftRegistration().value())));
    }
}
