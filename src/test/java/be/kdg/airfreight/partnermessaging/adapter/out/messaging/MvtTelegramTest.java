package be.kdg.airfreight.partnermessaging.adapter.out.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MvtTelegramTest {
    @ParameterizedTest
    @CsvSource({
            "09,ED1200,2026-01-09T12:00:00,2026-01-09,2026-01-09T12:00:00",
            "09,AD2355,2026-01-10T00:02:00,2026-01-09,2026-01-09T23:55:00",
            "09,ED0015,2026-01-08T23:58:00,2026-01-09,2026-01-09T00:15:00",
            "09,ED0015,2026-01-09T23:58:00,2026-01-09,2026-01-10T00:15:00",
            "31,AD2355,2027-01-01T00:02:00,2026-12-31,2026-12-31T23:55:00",
            "31,AD2355,2026-02-01T00:02:00,2026-01-31,2026-01-31T23:55:00",
            "01,ED0000,2026-04-16T12:00:00,2026-05-01,2026-04-17T00:00:00"
    })
    void derivesTheFlightDateAndMovementTime(String day, String movement, String readAt, String date, String time) {
        MvtTelegram telegram = MvtTelegram.parse("MVT\nHJ221/" + day + ".SXBAA.BRU\n" + movement,
                LocalDateTime.parse(readAt));

        assertEquals(LocalDate.parse(date), telegram.scheduledDepartureDate());
        assertEquals(LocalDateTime.parse(time), telegram.movementTime());
    }

    @Test
    void rejectsMalformedTelegramsAndImpossibleDatesOrTimes() {
        LocalDateTime readAt = LocalDateTime.parse("2026-02-15T12:00:00");
        for (String text : List.of("MVT\nHJ221/30.SXBAA.BRU\nED1200", "MVT\nHJ221/15.SXBAA.BRU\nED2460",
                "MVT HJ221/15 ED1200")) {
            assertThrows(IllegalArgumentException.class, () -> MvtTelegram.parse(text, readAt));
        }
    }
}
