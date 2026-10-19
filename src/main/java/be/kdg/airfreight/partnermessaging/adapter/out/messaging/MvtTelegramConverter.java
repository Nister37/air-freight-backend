package be.kdg.airfreight.partnermessaging.adapter.out.messaging;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class MvtTelegramConverter implements MessageConverter {
    @Override
    public Message toMessage(Object object, MessageProperties properties) {
        String text = switch (object) {
            case MvtTelegram telegram -> telegram.toText();
            // History already contains the telegram text. Retries must not parse its dates again.
            case String savedBody -> savedBody;
            default -> throw new MessageConversionException("Expected an MVT telegram or its saved text");
        };
        properties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        return new Message(text.getBytes(StandardCharsets.UTF_8), properties);
    }

    @Override
    public Object fromMessage(Message message) {
        String text = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            return MvtTelegram.parse(text, LocalDateTime.now(ZoneOffset.UTC));
        } catch (IllegalArgumentException exception) {
            throw new MessageConversionException("Invalid IATA MVT telegram", exception);
        }
    }
}
