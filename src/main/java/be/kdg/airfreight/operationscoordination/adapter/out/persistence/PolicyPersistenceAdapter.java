package be.kdg.airfreight.operationscoordination.adapter.out.persistence;

import be.kdg.airfreight.common.OperationsPolicy;
import be.kdg.airfreight.operationscoordination.adapter.out.persistence.entity.PolicyJpaEntity;
import be.kdg.airfreight.operationscoordination.adapter.out.persistence.repository.PolicyJpaRepository;
import be.kdg.airfreight.operationscoordination.port.out.PolicyStorePort;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
class PolicyPersistenceAdapter implements PolicyStorePort {
    private final PolicyJpaRepository repository;
    PolicyPersistenceAdapter(PolicyJpaRepository repository) { this.repository = repository; }
    public Optional<OperationsPolicy> load() { return repository.findById(PolicyJpaEntity.POLICY_ID).map(this::toDomain); }
    public void save(OperationsPolicy policy) {
        PolicyJpaEntity entity = repository.findById(PolicyJpaEntity.POLICY_ID).orElseGet(PolicyJpaEntity::new);
        updateEntity(entity, policy);
        repository.saveAndFlush(entity);
    }

    private void updateEntity(PolicyJpaEntity entity, OperationsPolicy policy) {
        entity.setOffloadingConstraint(policy.offloadingConstraint());
        entity.setLoadingWindowOpeningMinutes(policy.loadingWindowOpeningMinutes());
        entity.setLoadingWindowClosingMinutes(policy.loadingWindowClosingMinutes());
        entity.setMaximumLateralWeightDifferencePercent(policy.maximumLateralWeightDifferencePercent());
        entity.setFlightRetentionDays(policy.flightRetentionDays());
        entity.setActualTimeToleranceMinutes(policy.actualTimeToleranceMinutes());
    }

    private OperationsPolicy toDomain(PolicyJpaEntity entity) {
        return new OperationsPolicy(entity.getOffloadingConstraint(), entity.getLoadingWindowOpeningMinutes(),
                entity.getLoadingWindowClosingMinutes(), entity.getMaximumLateralWeightDifferencePercent(),
                entity.getFlightRetentionDays(), entity.getActualTimeToleranceMinutes());
    }
}
