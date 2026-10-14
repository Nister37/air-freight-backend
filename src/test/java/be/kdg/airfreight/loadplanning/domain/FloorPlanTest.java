package be.kdg.airfreight.loadplanning.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static be.kdg.airfreight.loadplanning.domain.FloorPlans.B747_400F;
import static be.kdg.airfreight.loadplanning.domain.FloorPlans.pos;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloorPlanTest {

    private static List<String> codes(List<Position> route) {
        return route.stream().map(Position::code).toList();
    }

    @Test
    void has34PositionsWithDoorAtRL() {
        assertThat(B747_400F.positions()).hasSize(34);
        assertThat(B747_400F.doorPosition().code()).isEqualTo("RL");
    }

    @Test
    void findsPositionsByCode() {
        assertThat(B747_400F.find("CL")).isPresent();
        assertThat(B747_400F.find("A1")).isPresent();
        assertThat(B747_400F.find("X")).isPresent();
        assertThat(B747_400F.find("ZZ")).isEmpty();
    }

    @Test
    void leftRowWalksTowardsTheDoor() {
        assertThat(B747_400F.routesToDoor(pos("TL")))
                .singleElement().satisfies(r -> assertThat(codes(r)).containsExactly("SL", "RL"));
    }

    @Test
    void rightRowCrossesToTheDoor() {
        assertThat(B747_400F.routesToDoor(pos("TR")))
                .singleElement().satisfies(r -> assertThat(codes(r)).containsExactly("SR", "RR", "RL"));
    }

    @Test
    void doorPositionHasNothingInTheWay() {
        assertThat(B747_400F.routesToDoor(pos("RL"))).singleElement().satisfies(r -> assertThat(r).isEmpty());
    }

    @Test
    void noseNeedsBothCPositionsThenLeftOrRight() {
        List<List<Position>> routes = B747_400F.routesToDoor(pos("A1"));

        assertThat(routes).hasSize(2);
        assertThat(codes(routes.get(0))).startsWith("CL", "CR").endsWith("RL").doesNotContain("DR");
        assertThat(codes(routes.get(1))).startsWith("CL", "CR").endsWith("RR", "RL").doesNotContain("DL");
    }

    @Test
    void tailNeedsBothUPositionsThenLeftOrRight() {
        List<List<Position>> routes = B747_400F.routesToDoor(pos("X"));

        assertThat(codes(routes.get(0))).containsExactly("UL", "UR", "TL", "SL", "RL");
        assertThat(codes(routes.get(1))).containsExactly("UL", "UR", "TR", "SR", "RR", "RL");
    }

    @Test
    void unknownPositionIsRejected() {
        assertThatThrownBy(() -> B747_400F.routesToDoor(new Position(Side.TAIL, new Slot("Z"))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
