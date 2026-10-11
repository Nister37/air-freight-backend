package be.kdg.airfreight;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AirFreightApplicationTests {
    @Autowired UserDetailsService users;
    @Autowired JdbcTemplate jdbc;

    @Test
    void contextLoads() {
    }

    @Test
    void localFixturesAreDisabledWithoutTheLocalProfile() {
        assertThat(jdbc.queryForObject("select count(*) from fo_flight", Integer.class)).isZero();
        assertThatThrownBy(() -> users.loadUserByUsername("planner"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
