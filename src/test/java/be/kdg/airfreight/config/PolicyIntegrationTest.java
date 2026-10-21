package be.kdg.airfreight.config;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.flightoperations.FlightOperationsApi;
import be.kdg.airfreight.flightoperations.domain.FlightPolicy;
import be.kdg.airfreight.flightoperations.port.in.usecase.GetFlightPolicyQuery;
import be.kdg.airfreight.loadplanning.domain.LoadPolicy;
import be.kdg.airfreight.loadplanning.domain.Severity;
import be.kdg.airfreight.loadplanning.port.in.usecase.GetLoadPolicyQuery;
import be.kdg.airfreight.loadplanning.port.in.usecase.GetLoadQuery;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class PolicyIntegrationTest {
    @Autowired private WebApplicationContext context;
    @Autowired private Filter springSecurityFilterChain;
    @Autowired private GetFlightPolicyQuery flightPolicy;
    @Autowired private GetLoadPolicyQuery loadPolicy;
    @Autowired private FlightOperationsApi flights;
    @Autowired private GetLoadQuery loads;
    @Autowired private JdbcTemplate jdbc;
    @MockitoBean private JwtDecoder decoder;
    @MockitoBean(name = "rabbitTemplate") private RabbitTemplate rabbit;
    @MockitoBean(name = "mvtRabbitTemplate") private RabbitTemplate telegrams;
    private MockMvc mvc;

    @BeforeEach
    void configureHttp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        when(decoder.decode(anyString())).thenAnswer(invocation -> {
            String role = invocation.getArgument(0);
            return Jwt.withTokenValue(role).header("alg", "RS256").subject("policy-test")
                    .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60))
                    .claim("preferred_username", "policy-test")
                    .claim("realm_access", Map.of("roles", List.of(role))).build();
        });
    }

    @Test
    void adminUpdatesArePersistedAndApplyToExistingFlightsAndLoads() throws Exception {
        assertEquals(FlightPolicy.defaults(), flightPolicy.currentPolicy());
        assertEquals(LoadPolicy.defaults(), loadPolicy.currentPolicy());
        LocalDateTime std = LocalDateTime.now(ZoneOffset.UTC).withNano(0).minusMinutes(8);
        var schedule = new FlightSchedule(std.toLocalDate(), std.toLocalDate(), Set.of(std.getDayOfWeek()),
                std.toLocalTime(), std.plusHours(2).toLocalTime(), new FlightNumber("HJ607"),
                new Airport("BRU"), new Airport("ATH"), new AircraftRegistration("SXBAA"));
        FlightId id = flights.announceFlights(schedule).getFirst().id();
        await().atMost(Duration.ofSeconds(10)).ignoreExceptions()
                .untilAsserted(() -> assertNotNull(loads.getLoad(id)));
        jdbc.update("UPDATE fo_flight SET final_load_received = TRUE WHERE id = ?", id.key());
        String departure = "{\"actualTime\":\"" + std.plusMinutes(8) + "\"}";
        mvc.perform(post("/api/flights/HJ607/{date}/BRU/departure", id.scheduledDepartureDate())
                        .header("Authorization", "Bearer flight-planner")
                        .contentType(MediaType.APPLICATION_JSON).content(departure))
                .andExpect(status().isConflict());

        mvc.perform(put("/api/flights/policy").header("Authorization", "Bearer admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flightRetentionDays\":11,\"actualTimeToleranceMinutes\":12}"))
                .andExpect(status().isOk());
        assertEquals(new FlightPolicy(11, 12), flightPolicy.currentPolicy());
        assertEquals(LoadPolicy.defaults(), loadPolicy.currentPolicy());

        mvc.perform(put("/api/loads/policy").header("Authorization", "Bearer admin")
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"offloadingConstraint":"SOFT","loadingWindowOpeningMinutes":600,
                                 "loadingWindowClosingMinutes":30,"maximumLateralWeightDifferencePercent":8}
                                """))
                .andExpect(status().isOk());
        assertEquals(new LoadPolicy(Severity.SOFT, 600, 30, new BigDecimal("8")), loadPolicy.currentPolicy());
        assertEquals(11, jdbc.queryForObject("SELECT flight_retention_days FROM fo_policy", Integer.class));
        assertEquals("SOFT", jdbc.queryForObject("SELECT offloading_constraint FROM lp_policy", String.class));
        assertEquals(std.minusMinutes(600), loads.getLoad(id).loadingWindowOpensAt());
        assertEquals(std.minusMinutes(30), loads.getLoad(id).loadingWindowClosesAt());

        mvc.perform(post("/api/flights/HJ607/{date}/BRU/departure", id.scheduledDepartureDate())
                        .header("Authorization", "Bearer flight-planner")
                        .contentType(MediaType.APPLICATION_JSON).content(departure))
                .andExpect(status().isOk());
        assertEquals(std.plusMinutes(8), flights.getFlightFacts(id).actualDeparture());
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                verify(telegrams).convertAndSend(eq("airfreight.partner-messages"), eq("mvt.ad"),
                        anyString(), any(MessagePostProcessor.class)));
    }

    @Test
    void policyEndpointsRequireAuthenticationAndAdminUpdatesAndRejectInvalidValues() throws Exception {
        for (String endpoint : List.of("/api/flights/policy", "/api/loads/policy")) {
            mvc.perform(get(endpoint)).andExpect(status().isUnauthorized());
            for (String role : List.of("flight-planner", "load-planner")) {
                mvc.perform(get(endpoint).header("Authorization", "Bearer " + role)).andExpect(status().isOk());
                mvc.perform(put(endpoint).header("Authorization", "Bearer " + role)
                                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                        .andExpect(status().isForbidden());
            }
            mvc.perform(put(endpoint).header("Authorization", "Bearer admin")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(put("/api/flights/policy").header("Authorization", "Bearer admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flightRetentionDays\":0,\"actualTimeToleranceMinutes\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/loads/policy").header("Authorization", "Bearer admin")
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"offloadingConstraint":"HARD","loadingWindowOpeningMinutes":20,
                                 "loadingWindowClosingMinutes":30,"maximumLateralWeightDifferencePercent":-1}
                                """))
                .andExpect(status().isBadRequest());
    }
}
