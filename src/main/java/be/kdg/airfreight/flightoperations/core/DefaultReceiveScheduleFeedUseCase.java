package be.kdg.airfreight.flightoperations.core;

import be.kdg.airfreight.flightoperations.port.in.command.ScheduleFeedCommand;
import be.kdg.airfreight.flightoperations.port.in.usecase.ReceiveScheduleFeedUseCase;
import be.kdg.airfreight.flightoperations.port.out.ScheduleFeedReceiptPort;

import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultReceiveScheduleFeedUseCase implements ReceiveScheduleFeedUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultReceiveScheduleFeedUseCase.class);
    private final FlightAnnouncement flightAnnouncement;
    private final ScheduleFeedReceiptPort receipts;

    DefaultReceiveScheduleFeedUseCase(FlightAnnouncement flightAnnouncement, ScheduleFeedReceiptPort receipts) {
        this.flightAnnouncement = flightAnnouncement;
        this.receipts = receipts;
    }

    @Override
    public void receiveScheduleFeed(ScheduleFeedCommand command) {
        if (receipts.contains(command.messageId())) {
            LOGGER.info("Duplicate SSM message ignored: {}", command.messageId());
            return;
        }
        flightAnnouncement.announce(command.schedule());
        receipts.record(command.messageId());
    }
}
