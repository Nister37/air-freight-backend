package be.kdg.airfreight.operationscoordination.adapter.out.persistence.entity;

import be.kdg.airfreight.common.OperationsPolicy;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "oc_policy")
public class PolicyJpaEntity {
    public static final int POLICY_ID = 1;

    @Id private int id = POLICY_ID;
    @Version private Long version;
    @Enumerated(EnumType.STRING) private OperationsPolicy.Severity offloadingConstraint;
    private int loadingWindowOpeningMinutes;
    private int loadingWindowClosingMinutes;
    @Column(precision = 19, scale = 6) private BigDecimal maximumLateralWeightDifferencePercent;
    private int flightRetentionDays;
    private int actualTimeToleranceMinutes;

    public PolicyJpaEntity() { }

    public OperationsPolicy.Severity getOffloadingConstraint() { return offloadingConstraint; }
    public int getLoadingWindowOpeningMinutes() { return loadingWindowOpeningMinutes; }
    public int getLoadingWindowClosingMinutes() { return loadingWindowClosingMinutes; }
    public BigDecimal getMaximumLateralWeightDifferencePercent() { return maximumLateralWeightDifferencePercent; }
    public int getFlightRetentionDays() { return flightRetentionDays; }
    public int getActualTimeToleranceMinutes() { return actualTimeToleranceMinutes; }

    public void setOffloadingConstraint(OperationsPolicy.Severity severity) { this.offloadingConstraint = severity; }
    public void setLoadingWindowOpeningMinutes(int minutes) { this.loadingWindowOpeningMinutes = minutes; }
    public void setLoadingWindowClosingMinutes(int minutes) { this.loadingWindowClosingMinutes = minutes; }
    public void setMaximumLateralWeightDifferencePercent(BigDecimal percent) {
        this.maximumLateralWeightDifferencePercent = percent;
    }
    public void setFlightRetentionDays(int days) { this.flightRetentionDays = days; }
    public void setActualTimeToleranceMinutes(int minutes) { this.actualTimeToleranceMinutes = minutes; }
}
