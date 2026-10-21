package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.CargoBooking;
import be.kdg.airfreight.loadplanning.port.in.usecase.BookCargoUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class DefaultBookCargoUseCase implements BookCargoUseCase {
    private final CargoBookingHandler cargoBooking;

    DefaultBookCargoUseCase(CargoBookingHandler cargoBooking) {
        this.cargoBooking = cargoBooking;
    }

    @Override
    public void bookCargo(CargoBooking booking) {
        cargoBooking.book(booking);
    }
}
