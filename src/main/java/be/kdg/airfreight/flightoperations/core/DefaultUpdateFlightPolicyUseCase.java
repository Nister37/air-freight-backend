package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.flightoperations.domain.FlightPolicy;
import be.kdg.airfreight.flightoperations.port.in.command.UpdateFlightPolicyCommand;
import be.kdg.airfreight.flightoperations.port.in.usecase.UpdateFlightPolicyUseCase;
import be.kdg.airfreight.flightoperations.port.out.FlightPolicyUpdatePort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultUpdateFlightPolicyUseCase implements UpdateFlightPolicyUseCase {
    private final FlightPolicyUpdatePort updatePort;

    DefaultUpdateFlightPolicyUseCase(FlightPolicyUpdatePort updatePort) {
        this.updatePort = updatePort;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_admin')")
    public FlightPolicy updatePolicy(UpdateFlightPolicyCommand command) {
        updatePort.update(command.policy());
        return command.policy();
    }
}
