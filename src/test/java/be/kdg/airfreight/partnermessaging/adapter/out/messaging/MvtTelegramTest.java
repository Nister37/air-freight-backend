package be.kdg.airfreight.partnermessaging.adapter.out.messaging;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.*;
import static org.assertj.core.api.Assertions.*;

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
    void derivesNearestDatesAndPrefersFutureOnTie(String day, String movement, String readAt, String date, String time) {
        MvtTelegram telegram = MvtTelegram.parse("MVT\nHJ221/" + day + ".SXBAA.BRU\n" + movement, LocalDateTime.parse(readAt));
        assertThat(telegram.scheduledDepartureDate()).isEqualTo(LocalDate.parse(date));
        assertThat(telegram.movementTime()).isEqualTo(LocalDateTime.parse(time));
    }

    @Test
    void rejectsMalformedTelegramAndImpossibleDatesOrTimes() {
        LocalDateTime now = LocalDateTime.parse("2026-02-15T12:00:00");
        for (String text : new String[]{"MVT\nHJ221/30.SXBAA.BRU\nED1200", "MVT\nHJ221/15.SXBAA.BRU\nED2460", "MVT HJ221/15 ED1200"}) {
            assertThatThrownBy(() -> MvtTelegram.parse(text, now)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void formatsExactlyThreeLinesWithUtcMinutes() {
        MvtTelegram telegram = new MvtTelegram(new FlightNumber("HJ221"), LocalDate.of(2026, 10, 9),
                new AircraftRegistration("SXBAA"), new Airport("BRU"), MessageKind.MVT_ED, LocalDateTime.parse("2026-10-09T06:45:00"));
        assertThat(telegram.toText()).isEqualTo("MVT\nHJ221/09.SXBAA.BRU\nED0645");
    }
}
