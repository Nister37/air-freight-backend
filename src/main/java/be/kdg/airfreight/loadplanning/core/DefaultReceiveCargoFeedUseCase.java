package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.loadplanning.port.in.command.CargoFeedCommand;
import be.kdg.airfreight.loadplanning.port.in.usecase.ReceiveCargoFeedUseCase;
import be.kdg.airfreight.loadplanning.port.out.CargoFeedReceiptPort;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultReceiveCargoFeedUseCase implements ReceiveCargoFeedUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultReceiveCargoFeedUseCase.class);
    private final CargoBookingHandler cargoBooking;
    private final CargoFeedReceiptPort receipts;

    DefaultReceiveCargoFeedUseCase(CargoBookingHandler cargoBooking, CargoFeedReceiptPort receipts) {
        this.cargoBooking = cargoBooking;
        this.receipts = receipts;
    }

    @Override
    public void receiveCargoFeed(CargoFeedCommand command) {
        if (receipts.contains(command.messageId())) {
            LOGGER.info("Duplicate ULD message ignored: {}", command.messageId());
            return;
        }
        // Resolve a day-only date once, after the receipt check, never again on redelivery.
        CargoBooking booking = new CargoBooking(command.messageId(), new FlightId(command.flightNumber(),
                command.departureDay().resolve(LocalDate.now(ZoneOffset.UTC)), command.departureAirport()),
                command.uldCode(), command.weight(), command.unit(), command.destination());
        cargoBooking.book(booking);
        receipts.record(booking.messageId());
    }
}
