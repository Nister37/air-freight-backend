package be.kdg.airfreight.loadplanning.domain.entity;

import be.kdg.airfreight.loadplanning.domain.vo.ULDCode;
import be.kdg.airfreight.loadplanning.domain.vo.Weight;

import be.kdg.airfreight.common.Airport;

import java.util.Objects;

public final class ULD {

    private final ULDCode code;
    private final Weight weight;
    private final Airport destination;
    private ULD(ULDCode code, Weight weight, Airport destination) {
        Objects.requireNonNull(code, "ULD code is required");
        Objects.requireNonNull(weight, "Weight is required");
        Objects.requireNonNull(destination, "Airport destination is required");
        this.code = code;
        this.weight = weight;
        this.destination = destination;
    }

    public static ULD of(ULDCode code, Weight weight, Airport destination) {
        return new ULD(code, weight, destination);
    }
    public ULDCode code(){
        return code;
    }
    public Weight weight(){
        return weight;
    }
    public Airport destination(){
        return destination;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ULD other && code.equals(other.code);
    }
    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
