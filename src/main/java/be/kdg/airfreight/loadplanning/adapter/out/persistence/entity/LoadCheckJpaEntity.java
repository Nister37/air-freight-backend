package be.kdg.airfreight.loadplanning.adapter.out.persistence.entity;

import be.kdg.airfreight.loadplanning.domain.CheckKind;
import be.kdg.airfreight.loadplanning.domain.Result;
import be.kdg.airfreight.loadplanning.domain.Severity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "lp_load_check")
public class LoadCheckJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID loadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckKind kind;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Result result;

    @Column(nullable = false, length = 4000)
    private String explanation;

    protected LoadCheckJpaEntity() { }

    public LoadCheckJpaEntity(UUID loadId, CheckKind kind, Severity severity, Result result, String explanation) {
        this.loadId = loadId;
        this.kind = kind;
        this.severity = severity;
        this.result = result;
        this.explanation = explanation;
    }

    public CheckKind getKind() { return kind; }
    public Severity getSeverity() { return severity; }
    public Result getResult() { return result; }
    public String getExplanation() { return explanation; }
}
