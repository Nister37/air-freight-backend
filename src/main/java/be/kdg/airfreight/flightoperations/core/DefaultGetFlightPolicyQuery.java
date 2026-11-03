package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.flightoperations.domain.vo.FlightPolicy;
import be.kdg.airfreight.flightoperations.port.in.usecase.GetFlightPolicyQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DefaultGetFlightPolicyQuery implements GetFlightPolicyQuery {
    private final FlightPolicyReader policyReader;

    DefaultGetFlightPolicyQuery(FlightPolicyReader policyReader) {
        this.policyReader = policyReader;
    }

    @Override
    public FlightPolicy currentPolicy() {
        return policyReader.current();
    }
}
