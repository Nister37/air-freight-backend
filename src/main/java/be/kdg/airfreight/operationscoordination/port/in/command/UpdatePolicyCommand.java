package be.kdg.airfreight.operationscoordination.port.in.command;

import be.kdg.airfreight.common.OperationsPolicy;
import java.util.Objects;

public record UpdatePolicyCommand(OperationsPolicy policy) {
    public UpdatePolicyCommand { Objects.requireNonNull(policy, "Policy is required"); }
}
