package be.kdg.airfreight.operationscoordination.adapter.in.rest;

import be.kdg.airfreight.common.OperationsPolicy;
import be.kdg.airfreight.operationscoordination.port.in.command.UpdatePolicyCommand;
import java.math.BigDecimal;

public record UpdatePolicyRequest(OperationsPolicy.Severity offloadingConstraint, Integer loadingWindowOpeningMinutes,
                            Integer loadingWindowClosingMinutes, BigDecimal maximumLateralWeightDifferencePercent,
                            Integer flightRetentionDays, Integer actualTimeToleranceMinutes) {
    public UpdatePolicyRequest {
        if (offloadingConstraint == null || loadingWindowOpeningMinutes == null || loadingWindowClosingMinutes == null
                || maximumLateralWeightDifferencePercent == null || flightRetentionDays == null || actualTimeToleranceMinutes == null) {
            throw new IllegalArgumentException("All six policy settings are required");
        }
    }

    UpdatePolicyCommand toCommand() {
        return new UpdatePolicyCommand(new OperationsPolicy(offloadingConstraint, loadingWindowOpeningMinutes,
                loadingWindowClosingMinutes, maximumLateralWeightDifferencePercent, flightRetentionDays, actualTimeToleranceMinutes));
    }
}
