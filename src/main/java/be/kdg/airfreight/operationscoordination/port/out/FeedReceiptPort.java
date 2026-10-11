package be.kdg.airfreight.operationscoordination.port.out;

import java.util.UUID;

public interface FeedReceiptPort {
    boolean contains(UUID messageId);
    void record(UUID messageId);
}
