package be.kdg.airfreight;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "local"})
class LocalDevelopmentTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;
    @MockitoBean RabbitTemplate rabbit;

    @Test
    void sampleDataCanBeReplayedWithoutOverwritingChanges() {
        assertThat(jdbc.queryForObject("select count(*) from fo_flight", Integer.class)).isEqualTo(3);
        jdbc.update("update oc_policy set actual_time_tolerance_minutes = 9 where id = 1");
        jdbc.update("update fo_flight set version = 3 where flight_number = 'HJ9001'");
        new ResourceDatabasePopulator(new ClassPathResource("data.sql")).execute(dataSource);
        assertThat(jdbc.queryForObject("select count(*) from fo_flight", Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("select actual_time_tolerance_minutes from oc_policy where id = 1", Integer.class)).isEqualTo(9);
        assertThat(jdbc.queryForObject("select version from fo_flight where flight_number = 'HJ9001'", Long.class)).isEqualTo(3);
    }

    @Test
    void plannerCanLogInUsingTheGeneratedFormAndCsrfToken() throws Exception {
        var loginPage = mvc.perform(get("/login")).andExpect(status().isOk()).andReturn();
        var session = (MockHttpSession) loginPage.getRequest().getSession(false);
        var login = mvc.perform(post("/login").session(session)
                        .param("username", "planner").param("password", "local123")
                        .param("_csrf", csrfFrom(loginPage.getResponse().getContentAsString())))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/")).andReturn();
        session = (MockHttpSession) login.getRequest().getSession(false);
        String date = LocalDate.now(ZoneOffset.UTC).minusDays(1).toString();
        mvc.perform(get("/api/flights/HJ9002/" + date + "/BRU").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.finalLoadReceived").value(true));
        var freshPage = mvc.perform(get("/login").session(session)).andExpect(status().isOk()).andReturn();
        mvc.perform(post("/api/flights/HJ9001/" + date + "/BRU/delays").session(session)
                        .header("X-CSRF-TOKEN", csrfFrom(freshPage.getResponse().getContentAsString()))
                        .contentType("application/json")
                        .content("{\"phase\":\"DEPARTURE\",\"code\":\"93\",\"minutes\":0,\"description\":\"Invalid\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/flights/HJ9001/" + date + "/BRU/delays").session(session)
                        .contentType("application/json")
                        .content("{\"phase\":\"DEPARTURE\",\"code\":\"93\",\"minutes\":5,\"description\":\"Test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loaderAndAdminHaveTheirOwnRolesAndBadPasswordsFail() throws Exception {
        for (String username : new String[]{"loader", "admin"}) {
            var login = mvc.perform(post("/login").with(csrf())
                            .param("username", username).param("password", "local123"))
                    .andExpect(redirectedUrl("/")).andReturn();
            var session = (MockHttpSession) login.getRequest().getSession(false);
            mvc.perform(get("/api/policy").session(session)).andExpect(status().isOk());
            mvc.perform(put("/api/policy").session(session).with(csrf()).contentType("application/json")
                            .content("{\"offloadingConstraint\":\"HARD\",\"loadingWindowOpeningMinutes\":720,"
                                    + "\"loadingWindowClosingMinutes\":20,\"maximumLateralWeightDifferencePercent\":10,"
                                    + "\"flightRetentionDays\":7,\"actualTimeToleranceMinutes\":5}"))
                    .andExpect(username.equals("admin") ? status().isOk() : status().isForbidden());
        }
        mvc.perform(post("/login").with(csrf()).param("username", "planner").param("password", "wrong"))
                .andExpect(redirectedUrl("/login?error"));
    }

    private String csrfFrom(String html) {
        var matcher = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(html);
        assertThat(matcher.find()).as("Generated login page contains a CSRF token").isTrue();
        return matcher.group(1);
    }
}
