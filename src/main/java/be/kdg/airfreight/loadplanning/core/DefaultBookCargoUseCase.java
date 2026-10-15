package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.CargoBooking;
import be.kdg.airfreight.loadplanning.LoadNotFoundException;
import be.kdg.airfreight.loadplanning.domain.*;
import be.kdg.airfreight.loadplanning.port.in.usecase.BookCargoUseCase;
import be.kdg.airfreight.loadplanning.port.out.LoadLoadPort;
import be.kdg.airfreight.loadplanning.port.out.LoadUpdatePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;


@Service
@Transactional
class DefaultBookCargoUseCase implements BookCargoUseCase {

    private static final Logger LOGGER =  LoggerFactory.getLogger(DefaultBookCargoUseCase.class);

    private final LoadLoadPort loadPort;
    private final LoadUpdatePort updatePort;

    DefaultBookCargoUseCase(LoadLoadPort loadPort, LoadUpdatePort updatePort) {
        this.loadPort = loadPort;
        this.updatePort = updatePort;
    }

    @Override
    public void bookCargo(CargoBooking booking) {
        Load load = loadPort.load(booking.flightId())
                .orElseThrow(()-> new LoadNotFoundException("No load for flight " + booking.flightId().key()));

        ULD uld = ULD.of(
                new ULDCode(booking.uldCode()),
                new Weight(booking.weight(), WeightUnit.valueOf(booking.unit())),
                booking.destination());

        boolean booked = load.book(uld, LocalDateTime.now(ZoneOffset.UTC));

        if (!booked) {
            LOGGER.info("Ignored duplicate booking of ULD {} on flight {} (message {})",
                    booking.uldCode(), booking.flightId().key(), booking.messageId());
            return;
        }
        updatePort.update(load);
    }



}
