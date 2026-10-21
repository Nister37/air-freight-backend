package be.kdg.airfreight.loadplanning.adapter.out.persistence.entity;

import be.kdg.airfreight.loadplanning.domain.Severity;
import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "lp_policy")
public class LoadPolicyJpaEntity {
    public static final int POLICY_ID = 1;

    @Id private int id = POLICY_ID;
    @Version private Long version;
    @Enumerated(EnumType.STRING) private Severity offloadingConstraint;
    private int loadingWindowOpeningMinutes;
    private int loadingWindowClosingMinutes;
    @Column(precision = 19, scale = 6) private BigDecimal maximumLateralWeightDifferencePercent;

    protected LoadPolicyJpaEntity() {
    }

    public LoadPolicyJpaEntity(Severity offloadingConstraint, int loadingWindowOpeningMinutes,
                              int loadingWindowClosingMinutes, BigDecimal maximumLateralWeightDifferencePercent) {
        update(offloadingConstraint, loadingWindowOpeningMinutes, loadingWindowClosingMinutes, maximumLateralWeightDifferencePercent);
    }

    public void update(Severity offloadingConstraint, int loadingWindowOpeningMinutes,
                              int loadingWindowClosingMinutes, BigDecimal maximumLateralWeightDifferencePercent) {
        this.offloadingConstraint = offloadingConstraint;
        this.loadingWindowOpeningMinutes = loadingWindowOpeningMinutes;
        this.loadingWindowClosingMinutes = loadingWindowClosingMinutes;
        this.maximumLateralWeightDifferencePercent = maximumLateralWeightDifferencePercent;
    }

    public Severity getOffloadingConstraint() {
        return offloadingConstraint;
    }

    public int getLoadingWindowOpeningMinutes() {
        return loadingWindowOpeningMinutes;
    }

    public int getLoadingWindowClosingMinutes() {
        return loadingWindowClosingMinutes;
    }

    public BigDecimal getMaximumLateralWeightDifferencePercent() {
        return maximumLateralWeightDifferencePercent;
    }
}
