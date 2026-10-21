package be.kdg.airfreight.loadplanning.port.in.command;

import be.kdg.airfreight.loadplanning.domain.LoadPolicy;
import java.util.Objects;

public record UpdateLoadPolicyCommand(LoadPolicy policy) {
    public UpdateLoadPolicyCommand {
        Objects.requireNonNull(policy, "Policy is required");
    }
}
