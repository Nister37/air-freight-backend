package be.kdg.airfreight.flightoperations.port.out;

import java.util.UUID;

public interface ScheduleFeedReceiptPort {
    boolean contains(UUID messageId);
    void record(UUID messageId);
}
