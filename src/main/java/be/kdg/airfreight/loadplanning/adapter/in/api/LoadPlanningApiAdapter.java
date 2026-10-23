package be.kdg.airfreight.loadplanning.adapter.in.api;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.loadplanning.LoadPlanningApi;
import be.kdg.airfreight.loadplanning.port.in.command.FinaliseLoadCommand;
import be.kdg.airfreight.loadplanning.port.in.command.PlaceULDCommand;
import be.kdg.airfreight.loadplanning.port.in.usecase.FinaliseLoadUseCase;
import be.kdg.airfreight.loadplanning.port.in.usecase.PlaceULDUseCase;
import org.springframework.stereotype.Component;

@Component
class LoadPlanningApiAdapter implements LoadPlanningApi {

    private final PlaceULDUseCase placeULD;
    private final FinaliseLoadUseCase finaliseLoad;

    LoadPlanningApiAdapter(PlaceULDUseCase placeULD, FinaliseLoadUseCase finaliseLoad) {
        this.placeULD = placeULD;
        this.finaliseLoad = finaliseLoad;
    }

    @Override
    public LoadFacts placeULD(FlightId id, String uldCode, String position) {
        return placeULD.placeULD(new PlaceULDCommand(id, uldCode, position));
    }

    @Override
    public LoadFacts finaliseLoad(FlightId id, boolean confirmWarnings) {
        return finaliseLoad.finaliseLoad(new FinaliseLoadCommand(id, confirmWarnings));
    }
}
