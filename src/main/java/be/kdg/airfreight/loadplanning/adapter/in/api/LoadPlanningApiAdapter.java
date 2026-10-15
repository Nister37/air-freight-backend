package be.kdg.airfreight.loadplanning.adapter.in.api;

import be.kdg.airfreight.common.CargoBooking;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.loadplanning.LoadPlanningApi;
import be.kdg.airfreight.loadplanning.port.in.command.FinaliseLoadCommand;
import be.kdg.airfreight.loadplanning.port.in.command.PlaceULDCommand;
import be.kdg.airfreight.loadplanning.port.in.command.RemovePlacementCommand;
import be.kdg.airfreight.loadplanning.port.in.usecase.BookCargoUseCase;
import be.kdg.airfreight.loadplanning.port.in.usecase.CheckLoadUseCase;
import be.kdg.airfreight.loadplanning.port.in.usecase.FinaliseLoadUseCase;
import be.kdg.airfreight.loadplanning.port.in.usecase.GetLoadUseCase;
import be.kdg.airfreight.loadplanning.port.in.usecase.PlaceULDUseCase;
import be.kdg.airfreight.loadplanning.port.in.usecase.RemovePlacementUseCase;
import org.springframework.stereotype.Component;

@Component
class LoadPlanningApiAdapter implements LoadPlanningApi {

    private final BookCargoUseCase bookCargo;
    private final GetLoadUseCase getLoad;
    private final PlaceULDUseCase placeULD;
    private final RemovePlacementUseCase removePlacement;
    private final CheckLoadUseCase checkLoad;
    private final FinaliseLoadUseCase finaliseLoad;

    LoadPlanningApiAdapter(BookCargoUseCase bookCargo, GetLoadUseCase getLoad, PlaceULDUseCase placeULD,
                           RemovePlacementUseCase removePlacement, CheckLoadUseCase checkLoad,
                           FinaliseLoadUseCase finaliseLoad) {
        this.bookCargo = bookCargo;
        this.getLoad = getLoad;
        this.placeULD = placeULD;
        this.removePlacement = removePlacement;
        this.checkLoad = checkLoad;
        this.finaliseLoad = finaliseLoad;
    }

    @Override
    public void bookCargo(CargoBooking booking){
        bookCargo.bookCargo(booking);
    }

    @Override
    public LoadFacts getLoad(FlightId id) {
        return getLoad.getLoad(id);
    }

    @Override
    public LoadFacts placeULD(FlightId id, String uldCode, String position) {
        return placeULD.placeULD(new PlaceULDCommand(id, uldCode, position));
    }

    @Override
    public LoadFacts removePlacement(FlightId id, String uldCode) {
        return removePlacement.removePlacement(new RemovePlacementCommand(id, uldCode));
    }

    @Override
    public LoadFacts checkLoad(FlightId id) {
        return checkLoad.checkLoad(id);
    }

    @Override
    public LoadFacts finaliseLoad(FlightId id, boolean confirmWarnings) {
        return finaliseLoad.finaliseLoad(new FinaliseLoadCommand(id, confirmWarnings));
    }


}
