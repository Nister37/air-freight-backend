package be.kdg.airfreight.partnermessaging.adapter.out.messaging;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.partnermessaging.domain.MessageKind;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.temporal.ChronoUnit;
import static org.assertj.core.api.Assertions.*;

class MvtTelegramConverterTest {
    private final MvtTelegramConverter converter = new MvtTelegramConverter();

    @Test
    void convertsTelegramObjectsToPlainTextAndReadsTheirUtcDates() {
        LocalDateTime movement = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);
        MvtTelegram telegram = new MvtTelegram(new FlightNumber("HJ221"), movement.toLocalDate(),
                new AircraftRegistration("SXBAA"), new Airport("BRU"), MessageKind.MVT_AD, movement);
        MessageProperties properties = new MessageProperties();
        properties.setMessageId("existing-message-id");

        Message wire = converter.toMessage(telegram, properties);

        assertThat(new String(wire.getBody(), StandardCharsets.UTF_8)).isEqualTo(telegram.toText());
        assertThat(wire.getMessageProperties().getContentType()).isEqualTo("text/plain");
        assertThat(wire.getMessageProperties().getContentEncoding()).isEqualTo("UTF-8");
        assertThat(wire.getMessageProperties().getMessageId()).isEqualTo("existing-message-id");
        assertThat(converter.fromMessage(wire)).isEqualTo(telegram);
    }

    @Test
    void preservesSavedTelegramTextWithoutReadingItsDatesAgain() {
        String savedBody = "MVT\r\nHJ221/31.SXBAA.BRU\r\nAD2355";

        Message wire = converter.toMessage(savedBody, new MessageProperties());

        assertThat(wire.getBody()).isEqualTo(savedBody.getBytes(StandardCharsets.UTF_8));
        assertThat(wire.getMessageProperties().getContentType()).isEqualTo("text/plain");
    }

    @Test
    void malformedIncomingTelegramsAreAmqpConversionFailures() {
        for (String text : new String[]{"MVT HJ221/15 AD1200", "MVT\nHJ221/15.SXBAA.BRU\nAD2460"}) {
            Message wire = new Message(text.getBytes(StandardCharsets.UTF_8), new MessageProperties());
            assertThatThrownBy(() -> converter.fromMessage(wire))
                    .isInstanceOf(MessageConversionException.class)
                    .hasCauseInstanceOf(IllegalArgumentException.class);
        }
    }
}
