package be.kdg.airfreight;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AirFreightApplicationTests {
    @Autowired UserDetailsService users;
    @Autowired JdbcTemplate jdbc;
    @Autowired JacksonJsonMessageConverter jsonMessageConverter;
    @Autowired RabbitTemplate rabbit;
    @Autowired ApplicationContext context;

    @Test
    void contextLoads() {
    }

    @Test
    void jsonRemainsTheDefaultAndTelegramsHaveTheirOwnConverter() {
        assertThat(context.getBeansOfType(RabbitTemplate.class)).containsOnlyKeys("rabbitTemplate", "mvtRabbitTemplate");
        assertThat(rabbit.getMessageConverter()).isSameAs(jsonMessageConverter);
        assertThat(context.getBean("mvtRabbitTemplate", RabbitTemplate.class).getMessageConverter())
                .isSameAs(context.getBean("mvtTelegramConverter"));
        assertThat(context.getBean(org.springframework.amqp.support.converter.MessageConverter.class))
                .isSameAs(jsonMessageConverter);
    }

    @Test
    void localFixturesAreDisabledWithoutTheLocalProfile() {
        assertThat(jdbc.queryForObject("select count(*) from fo_flight", Integer.class)).isZero();
        assertThatThrownBy(() -> users.loadUserByUsername("planner"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
