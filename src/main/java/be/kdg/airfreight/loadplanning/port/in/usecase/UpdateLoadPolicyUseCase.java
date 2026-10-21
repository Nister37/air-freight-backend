package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.loadplanning.domain.LoadPolicy;
import be.kdg.airfreight.loadplanning.port.in.command.UpdateLoadPolicyCommand;

public interface UpdateLoadPolicyUseCase {
    LoadPolicy updatePolicy(UpdateLoadPolicyCommand command);
}
