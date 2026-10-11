package be.kdg.airfreight.operationscoordination.port.out;

import be.kdg.airfreight.common.CargoBooking;

public interface LoadRequestPort { void bookCargo(CargoBooking booking); }
