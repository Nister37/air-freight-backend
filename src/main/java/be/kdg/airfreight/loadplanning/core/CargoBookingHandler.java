package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.CargoBooking;
import be.kdg.airfreight.loadplanning.domain.Load;
import be.kdg.airfreight.loadplanning.domain.ULD;
import be.kdg.airfreight.loadplanning.domain.ULDCode;
import be.kdg.airfreight.loadplanning.domain.Weight;
import be.kdg.airfreight.loadplanning.domain.enums.WeightUnit;
import be.kdg.airfreight.loadplanning.port.out.LoadUpdatePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
class CargoBookingHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CargoBookingHandler.class);
    private final LoadLookup loadLookup;
    private final List<LoadUpdatePort> updatePorts;

    CargoBookingHandler(LoadLookup loadLookup, List<LoadUpdatePort> updatePorts) {
        this.loadLookup = loadLookup;
        this.updatePorts = List.copyOf(updatePorts);
    }

    void book(CargoBooking booking) {
        Load load = loadLookup.required(booking.flightId());
        ULD uld = ULD.of(new ULDCode(booking.uldCode()),
                new Weight(booking.weight(), WeightUnit.valueOf(booking.unit())), booking.destination());
        boolean booked = load.book(uld, LocalDateTime.now(ZoneOffset.UTC));
        if (!booked) {
            LOGGER.info("Ignored duplicate booking of ULD {} on flight {} (message {})",
                    booking.uldCode(), booking.flightId().key(), booking.messageId());
            return;
        }
        updatePorts.forEach(port -> port.update(load));
    }
}
