package be.kdg.airfreight.partnermessaging.adapter.config;

import be.kdg.airfreight.partnermessaging.adapter.out.messaging.MvtTelegramConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class PartnerRabbitConfiguration {
    @Bean
    MvtTelegramConverter mvtTelegramConverter() {
        return new MvtTelegramConverter();
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
