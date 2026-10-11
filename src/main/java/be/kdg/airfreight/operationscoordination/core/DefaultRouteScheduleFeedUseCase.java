package be.kdg.airfreight.operationscoordination.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.ScheduleFeedCommand;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RouteScheduleFeedUseCase;
import be.kdg.airfreight.operationscoordination.port.out.*;
import java.time.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultRouteScheduleFeedUseCase implements RouteScheduleFeedUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRouteScheduleFeedUseCase.class);
    private final FlightRequestPort flights;
    private final FeedReceiptPort receipts;

    DefaultRouteScheduleFeedUseCase(FlightRequestPort flights, FeedReceiptPort receipts) {
        this.flights = flights;
        this.receipts = receipts;
    }

    @Override
    public void routeScheduleFeed(ScheduleFeedCommand command) {
        if (receipts.contains(command.messageId())) {
            LOGGER.info("Duplicate SSM message ignored: {}", command.messageId());
            return;
        }
        flights.announceFlights(command.schedule());
        receipts.record(command.messageId());
    }
}
