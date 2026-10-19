package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.LoadFacts;
import be.kdg.airfreight.loadplanning.port.in.usecase.GetLoadUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
class DefaultGetLoadUseCase implements GetLoadUseCase {

    private final LoadLookup loadLookup;

    DefaultGetLoadUseCase(LoadLookup loadLookup) {
        this.loadLookup = loadLookup;
    }

    @Override
    public LoadFacts getLoad(FlightId id){
        return loadLookup.required(id).facts();
    }

}
