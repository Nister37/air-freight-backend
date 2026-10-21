package be.kdg.airfreight.flightoperations.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "fo_policy")
public class FlightPolicyJpaEntity {
    public static final int POLICY_ID = 1;

    @Id private int id = POLICY_ID;
    @Version private Long version;
    private int flightRetentionDays;
    private int actualTimeToleranceMinutes;

    protected FlightPolicyJpaEntity() {
    }

    public FlightPolicyJpaEntity(int flightRetentionDays, int actualTimeToleranceMinutes) {
        update(flightRetentionDays, actualTimeToleranceMinutes);
    }

    public void update(int flightRetentionDays, int actualTimeToleranceMinutes) {
        this.flightRetentionDays = flightRetentionDays;
        this.actualTimeToleranceMinutes = actualTimeToleranceMinutes;
    }

    public int getFlightRetentionDays() {
        return flightRetentionDays;
    }

    public int getActualTimeToleranceMinutes() {
        return actualTimeToleranceMinutes;
    }
}
