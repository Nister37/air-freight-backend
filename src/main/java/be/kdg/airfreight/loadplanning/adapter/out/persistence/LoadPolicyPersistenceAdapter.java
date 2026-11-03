package be.kdg.airfreight.loadplanning.adapter.out.persistence;

import be.kdg.airfreight.loadplanning.domain.vo.LoadPolicy;
import be.kdg.airfreight.loadplanning.adapter.out.persistence.entity.LoadPolicyJpaEntity;
import be.kdg.airfreight.loadplanning.adapter.out.persistence.repository.LoadPolicyJpaRepository;
import be.kdg.airfreight.loadplanning.port.out.LoadPolicyLoadPort;
import be.kdg.airfreight.loadplanning.port.out.LoadPolicyUpdatePort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class LoadPolicyPersistenceAdapter implements LoadPolicyLoadPort, LoadPolicyUpdatePort {
    private final LoadPolicyJpaRepository repository;

    LoadPolicyPersistenceAdapter(LoadPolicyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<LoadPolicy> load() {
        return repository.findById(LoadPolicyJpaEntity.POLICY_ID)
                .map(entity -> new LoadPolicy(entity.getOffloadingConstraint(), entity.getLoadingWindowOpeningMinutes(), entity.getLoadingWindowClosingMinutes(), entity.getMaximumLateralWeightDifferencePercent()));
    }

    @Override
    public void update(LoadPolicy policy) {
        LoadPolicyJpaEntity entity = repository.findById(LoadPolicyJpaEntity.POLICY_ID)
                .orElseGet(() -> new LoadPolicyJpaEntity(policy.offloadingConstraint(), policy.loadingWindowOpeningMinutes(), policy.loadingWindowClosingMinutes(), policy.maximumLateralWeightDifferencePercent()));
        entity.update(policy.offloadingConstraint(), policy.loadingWindowOpeningMinutes(), policy.loadingWindowClosingMinutes(), policy.maximumLateralWeightDifferencePercent());
        repository.saveAndFlush(entity);
    }
}
