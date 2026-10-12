package be.kdg.airfreight.loadplanning.domain;

import java.util.Objects;

public final class AircraftType {

    private final String typeCode;
    private final FloorPlan floorPlan;

    private AircraftType(String typeCode, FloorPlan floorPlan) {
        Objects.requireNonNull(typeCode, "type code is required");
        Objects.requireNonNull(floorPlan,"floor plan is required");
        if (typeCode.isBlank()){
            throw new IllegalArgumentException("type code cannot be blank");
        }
        this.typeCode = typeCode;
        this.floorPlan = floorPlan;
    }

    public static AircraftType restore(String typeCode, FloorPlan floorPlan){
        return new AircraftType(typeCode, floorPlan);
    }

    public String typeCode(){
        return typeCode;
    }

    public FloorPlan floorPlan(){
        return floorPlan;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AircraftType other && typeCode.equals(other.typeCode);
    }

    @Override
    public int hashCode() {
        return typeCode.hashCode();
    }

}
