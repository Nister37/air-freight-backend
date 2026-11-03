package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.loadplanning.domain.LoadFacts;
import be.kdg.airfreight.loadplanning.port.in.usecase.GetLoadQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
class DefaultGetLoadQuery implements GetLoadQuery {

    private final LoadReader loadReader;

    DefaultGetLoadQuery(LoadReader loadReader) {
        this.loadReader = loadReader;
    }

    @Override
    public LoadFacts getLoad(FlightId id){
        return loadReader.required(id).facts();
    }

}
