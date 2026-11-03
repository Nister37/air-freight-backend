package be.kdg.airfreight.loadplanning.domain.vo;

import be.kdg.airfreight.loadplanning.domain.enums.WeightUnit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeightTest {
    @Test
    void rejectsWeightsThatWouldBeRoundedOrOverflowInStorage() {
        for (String amount : List.of("0.0000001", "1.0000001", "10000000000000", "1E+13")) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Weight(new BigDecimal(amount), WeightUnit.KG), amount);
        }
    }

    @Test
    void acceptedWeightsSurviveStoragePrecisionWithoutChangingTheirValue() {
        for (String amount : List.of("0.000001", "9999999999999.999999", "1000.0000000", "1E+12")) {
            Weight original = new Weight(new BigDecimal(amount), WeightUnit.KG);
            BigDecimal stored = original.amount().setScale(6, RoundingMode.UNNECESSARY);

            assertTrue(stored.precision() <= 19, amount);
            Weight restored = new Weight(stored, original.unit());
            assertEquals(0, original.amount().compareTo(new BigDecimal(amount)), amount);
            assertEquals(original, restored, amount);
        }
    }
}
