package be.kdg.airfreight.loadplanning.domain;

import be.kdg.airfreight.common.AircraftRegistration;

import java.util.Objects;

public final class Aircraft {

    private final AircraftRegistration registration;
    private final String typeCode;

    private Aircraft(AircraftRegistration registration, String typeCode) {
        Objects.requireNonNull(registration, "registration is required");
        Objects.requireNonNull(typeCode, "typeCode is required");
        this.registration = registration;
        this.typeCode = typeCode;
    }
    public static Aircraft restore(AircraftRegistration registration, String typeCode) {
        return new Aircraft(registration, typeCode);
    }
    public AircraftRegistration registration() {
        return registration;
    }
    public String typeCode() {
        return typeCode;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Aircraft other && registration.equals(other.registration);
    }
    @Override
    public int hashCode() {
        return registration.hashCode();
    }

}
