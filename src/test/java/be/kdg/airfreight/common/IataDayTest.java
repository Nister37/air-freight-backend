package be.kdg.airfreight.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IataDayTest {
    @ParameterizedTest
    @CsvSource({"31,2026-01-01,2025-12-31", "1,2026-12-31,2027-01-01",
            "9,2026-04-24,2026-05-09", "11,2026-10-19,2026-10-11", "29,2028-02-28,2028-02-29"})
    void resolvesTheNearestDateAndChoosesTheFutureWhenTied(int day, LocalDate readOn, LocalDate expected) {
        assertEquals(expected, new IataDay(day).resolve(readOn));
    }

    @Test
    void rejectsInvalidDaysInsteadOfNormalisingThem() {
        assertThrows(IllegalArgumentException.class, () -> new IataDay(0));
        assertThrows(IllegalArgumentException.class, () -> new IataDay(32));
        assertThrows(IllegalArgumentException.class, () -> new IataDay(30).resolve(LocalDate.of(2026, 2, 28)));
    }
}
