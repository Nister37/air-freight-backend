package be.kdg.airfreight.operationscoordination.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.operationscoordination.port.in.command.CargoFeedCommand;
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
    public void routeCargoFeed(CargoFeedCommand command) {
        if (receipts.contains(command.messageId())) {
            LOGGER.info("Duplicate ULD message ignored: {}", command.messageId());
            return;
        }
        // Resolve a day-only date once, after the receipt check, never again on redelivery.
        CargoBooking booking = new CargoBooking(command.messageId(), new FlightId(command.flightNumber(),
                command.departureDay().resolve(LocalDate.now(ZoneOffset.UTC)), command.departureAirport()),
                command.uldCode(), command.weight(), command.unit(), command.destination());
        loads.bookCargo(booking);
        receipts.record(booking.messageId());
    }
}
