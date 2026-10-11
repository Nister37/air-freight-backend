package be.kdg.airfreight.operationscoordination.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RouteCargoFeedUseCase;
import be.kdg.airfreight.operationscoordination.port.out.*;
import java.time.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultRouteCargoFeedUseCase implements RouteCargoFeedUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRouteCargoFeedUseCase.class);
    private final LoadRequestPort loads;
    private final FeedReceiptPort receipts;

    DefaultRouteCargoFeedUseCase(LoadRequestPort loads, FeedReceiptPort receipts) {
        this.loads = loads;
        this.receipts = receipts;
    }

    @Override
    public void routeCargoFeed(CargoBooking booking) {
        if (receipts.contains(booking.messageId())) {
            LOGGER.info("Duplicate ULD message ignored: {}", booking.messageId());
            return;
        }
        loads.bookCargo(booking);
        receipts.record(booking.messageId());
    }
}
