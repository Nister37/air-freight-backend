package be.kdg.airfreight.loadplanning.port.out;

import java.util.UUID;

public interface CargoFeedReceiptPort {
    boolean contains(UUID messageId);
    void record(UUID messageId);
}
