package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.loadplanning.domain.LoadPolicy;
import be.kdg.airfreight.loadplanning.port.in.usecase.GetLoadPolicyQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DefaultGetLoadPolicyQuery implements GetLoadPolicyQuery {
    private final LoadPolicyReader policyReader;

    DefaultGetLoadPolicyQuery(LoadPolicyReader policyReader) {
        this.policyReader = policyReader;
    }

    @Override
    public LoadPolicy currentPolicy() {
        return policyReader.current();
    }
}
