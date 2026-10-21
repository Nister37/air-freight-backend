package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.loadplanning.domain.LoadPolicy;
import be.kdg.airfreight.loadplanning.port.in.command.UpdateLoadPolicyCommand;
import be.kdg.airfreight.loadplanning.port.in.usecase.UpdateLoadPolicyUseCase;
import be.kdg.airfreight.loadplanning.port.out.LoadPolicyUpdatePort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultUpdateLoadPolicyUseCase implements UpdateLoadPolicyUseCase {
    private final LoadPolicyUpdatePort updatePort;

    DefaultUpdateLoadPolicyUseCase(LoadPolicyUpdatePort updatePort) {
        this.updatePort = updatePort;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_admin')")
    public LoadPolicy updatePolicy(UpdateLoadPolicyCommand command) {
        updatePort.update(command.policy());
        return command.policy();
    }
}
