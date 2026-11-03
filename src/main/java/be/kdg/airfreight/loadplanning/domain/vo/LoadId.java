package be.kdg.airfreight.loadplanning.domain.vo;

import java.util.Objects;
import java.util.UUID;

public record LoadId(UUID value) {
    public LoadId {
        Objects.requireNonNull(value, "load id is required");
    }

    public static LoadId create(){
        return new LoadId(UUID.randomUUID());
    }
}
