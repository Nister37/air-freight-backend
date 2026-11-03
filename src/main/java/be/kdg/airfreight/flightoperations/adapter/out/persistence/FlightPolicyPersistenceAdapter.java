package be.kdg.airfreight.flightoperations.adapter.out.persistence;

import be.kdg.airfreight.flightoperations.domain.vo.FlightPolicy;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.entity.FlightPolicyJpaEntity;
import be.kdg.airfreight.flightoperations.adapter.out.persistence.repository.FlightPolicyJpaRepository;
import be.kdg.airfreight.flightoperations.port.out.FlightPolicyLoadPort;
import be.kdg.airfreight.flightoperations.port.out.FlightPolicyUpdatePort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class FlightPolicyPersistenceAdapter implements FlightPolicyLoadPort, FlightPolicyUpdatePort {
    private final FlightPolicyJpaRepository repository;

    FlightPolicyPersistenceAdapter(FlightPolicyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<FlightPolicy> load() {
        return repository.findById(FlightPolicyJpaEntity.POLICY_ID)
                .map(entity -> new FlightPolicy(entity.getFlightRetentionDays(), entity.getActualTimeToleranceMinutes()));
    }

    @Override
    public void update(FlightPolicy policy) {
        FlightPolicyJpaEntity entity = repository.findById(FlightPolicyJpaEntity.POLICY_ID)
                .orElseGet(() -> new FlightPolicyJpaEntity(policy.flightRetentionDays(), policy.actualTimeToleranceMinutes()));
        entity.update(policy.flightRetentionDays(), policy.actualTimeToleranceMinutes());
        repository.saveAndFlush(entity);
    }
}
