package be.kdg.airfreight.operationscoordination.adapter.out.api;

import be.kdg.airfreight.common.CargoBooking;
import be.kdg.airfreight.loadplanning.LoadPlanningApi;
import be.kdg.airfreight.operationscoordination.LoadPlanningUnavailableException;
import be.kdg.airfreight.operationscoordination.port.out.LoadRequestPort;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
class LoadRequestAdapter implements LoadRequestPort {
    private final List<LoadPlanningApi> loads;
    LoadRequestAdapter(List<LoadPlanningApi> loads) { this.loads = loads; }
    public void bookCargo(CargoBooking booking) {
        if (loads.isEmpty()) throw new LoadPlanningUnavailableException();
        loads.getFirst().bookCargo(booking);
    }
}
