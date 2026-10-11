package be.kdg.airfreight.operationscoordination.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.usecase.GetPolicyUseCase;
import be.kdg.airfreight.operationscoordination.port.out.PolicyStorePort;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DefaultGetPolicyUseCase implements GetPolicyUseCase {
    private final PolicyStorePort store;
    DefaultGetPolicyUseCase(PolicyStorePort store) { this.store = store; }

    @Override
    public OperationsPolicy currentPolicy() { return store.load().orElseGet(OperationsPolicy::defaults); }
}
