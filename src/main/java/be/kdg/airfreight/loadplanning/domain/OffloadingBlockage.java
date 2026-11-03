package be.kdg.airfreight.loadplanning.domain;

import be.kdg.airfreight.loadplanning.domain.vo.Position;
import be.kdg.airfreight.loadplanning.domain.vo.ULDCode;
import be.kdg.airfreight.loadplanning.domain.vo.ULDPlacement;

import java.util.List;
import java.util.Objects;

public record OffloadingBlockage(ULDCode uldCode, Position position, List<ULDPlacement> blockers) {
    public OffloadingBlockage {
        Objects.requireNonNull(uldCode);
        Objects.requireNonNull(position);
        blockers = List.copyOf(blockers);
    }
}
