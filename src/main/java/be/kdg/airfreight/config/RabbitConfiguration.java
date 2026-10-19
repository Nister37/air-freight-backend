package be.kdg.airfreight.config;

import be.kdg.airfreight.common.events.DomainEvent;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateConfigurer;
import org.springframework.context.annotation.*;
import tools.jackson.databind.json.JsonMapper;

@Configuration
class RabbitConfiguration {
    @Bean
    @Primary
    JacksonJsonMessageConverter jsonMessageConverter(JsonMapper mapper) {
        return new JacksonJsonMessageConverter(mapper, DomainEvent.class.getPackageName());
    }

    // Boot backs off when the telegram template exists, so configure the JSON default explicitly.
    @Bean
    @Primary
    RabbitTemplate rabbitTemplate(RabbitTemplateConfigurer configurer, ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate();
        configurer.configure(template, connectionFactory);
        return template;
    }
}
