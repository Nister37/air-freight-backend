package be.kdg.airfreight.operationscoordination.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.UpdatePolicyCommand;
import be.kdg.airfreight.operationscoordination.port.in.usecase.UpdatePolicyUseCase;
import be.kdg.airfreight.operationscoordination.port.out.PolicyStorePort;
import java.time.*;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultUpdatePolicyUseCase implements UpdatePolicyUseCase {
    private final PolicyStorePort store;
    DefaultUpdatePolicyUseCase(PolicyStorePort store) { this.store = store; }

    @Override
    @PreAuthorize("hasAuthority('ROLE_admin')")
    public OperationsPolicy updatePolicy(UpdatePolicyCommand command) {
        store.save(command.policy());
        return command.policy();
    }
}
