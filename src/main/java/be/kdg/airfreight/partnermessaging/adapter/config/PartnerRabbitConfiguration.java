package be.kdg.airfreight.partnermessaging.adapter.config;

import be.kdg.airfreight.common.FlightFacts;
import be.kdg.airfreight.partnermessaging.adapter.out.messaging.MvtTelegramConverter;
import be.kdg.airfreight.partnermessaging.adapter.out.messaging.MvtTelegramFormatter;
import be.kdg.airfreight.partnermessaging.adapter.out.messaging.TimedMvtMessageHandler;
import be.kdg.airfreight.partnermessaging.domain.enums.MessageKind;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
class PartnerRabbitConfiguration {
    @Bean
    MvtTelegramFormatter mvtTelegramFormatter() {
        return new MvtTelegramFormatter(List.of(
                new TimedMvtMessageHandler("ED", MessageKind.MVT_ED, flight -> flight.id().departureAirport()),
                new TimedMvtMessageHandler("EA", MessageKind.MVT_EA, FlightFacts::arrivalAirport),
                new TimedMvtMessageHandler("AD", MessageKind.MVT_AD, flight -> flight.id().departureAirport()),
                new TimedMvtMessageHandler("AA", MessageKind.MVT_AA, FlightFacts::arrivalAirport)));
    }

    @Bean
    MvtTelegramConverter mvtTelegramConverter(MvtTelegramFormatter formatter) {
        return new MvtTelegramConverter(formatter);
    }

    @Bean
    RabbitTemplate mvtRabbitTemplate(RabbitTemplateConfigurer configurer, ConnectionFactory connectionFactory,
                                     MvtTelegramConverter converter) {
        RabbitTemplate template = new RabbitTemplate();
        configurer.configure(template, connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
