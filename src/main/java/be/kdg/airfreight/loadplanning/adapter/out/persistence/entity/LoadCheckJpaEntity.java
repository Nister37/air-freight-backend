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

    public LoadCheckJpaEntity() { }

    public Long getId() { return id; }
    public UUID getLoadId() { return loadId; }
    public void setLoadId(UUID loadId) { this.loadId = loadId; }
    public CheckKind getKind() { return kind; }
    public void setKind(CheckKind kind) { this.kind = kind; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public Result getResult() { return result; }
    public void setResult(Result result) { this.result = result; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
