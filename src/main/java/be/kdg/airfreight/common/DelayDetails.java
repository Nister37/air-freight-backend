package be.kdg.airfreight.common;

import java.util.UUID;

public record DelayDetails(UUID id, String code, String description, int minutes, DelayPhase phase) { }
