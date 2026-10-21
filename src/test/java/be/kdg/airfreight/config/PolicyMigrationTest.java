package be.kdg.airfreight.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PolicyMigrationTest {
    @Test
    void startupMigratesLegacyValuesAndReceiptsWithoutOverwritingLaterChanges() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL", "sa", "")) {
            var dataSource = new SingleConnectionDataSource(connection, true);
            var jdbc = new JdbcTemplate(dataSource);
            jdbc.execute("""
                    CREATE TABLE oc_policy (id INTEGER PRIMARY KEY, version BIGINT,
                      offloading_constraint VARCHAR(255), loading_window_opening_minutes INTEGER,
                      loading_window_closing_minutes INTEGER, maximum_lateral_weight_difference_percent NUMERIC(19,6),
                      flight_retention_days INTEGER, actual_time_tolerance_minutes INTEGER)
                    """);
            jdbc.execute("CREATE TABLE oc_feed_receipt (message_id UUID PRIMARY KEY)");
            jdbc.update("INSERT INTO oc_policy VALUES (1, 3, 'SOFT', 600, 30, 8.5, 11, 9)");
            UUID legacyId = UUID.randomUUID();
            jdbc.update("INSERT INTO oc_feed_receipt VALUES (?)", legacyId);
            var schema = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));

            schema.execute(dataSource);

            assertEquals(11, jdbc.queryForObject("SELECT flight_retention_days FROM fo_policy", Integer.class));
            assertEquals(9, jdbc.queryForObject("SELECT actual_time_tolerance_minutes FROM fo_policy", Integer.class));
            assertEquals("SOFT", jdbc.queryForObject("SELECT offloading_constraint FROM lp_policy", String.class));
            assertEquals(600, jdbc.queryForObject("SELECT loading_window_opening_minutes FROM lp_policy", Integer.class));
            assertEquals(30, jdbc.queryForObject("SELECT loading_window_closing_minutes FROM lp_policy", Integer.class));
            assertEquals(0, new BigDecimal("8.5").compareTo(jdbc.queryForObject(
                    "SELECT maximum_lateral_weight_difference_percent FROM lp_policy", BigDecimal.class)));
            assertEquals(legacyId, jdbc.queryForObject("SELECT message_id FROM fo_feed_receipt", UUID.class));
            assertEquals(legacyId, jdbc.queryForObject("SELECT message_id FROM lp_feed_receipt", UUID.class));

            jdbc.update("UPDATE fo_policy SET flight_retention_days = 12");
            jdbc.update("UPDATE lp_policy SET maximum_lateral_weight_difference_percent = 9");
            jdbc.update("INSERT INTO fo_feed_receipt VALUES (?)", UUID.randomUUID());
            jdbc.update("INSERT INTO lp_feed_receipt VALUES (?)", UUID.randomUUID());
            schema.execute(dataSource);

            assertEquals(12, jdbc.queryForObject("SELECT flight_retention_days FROM fo_policy", Integer.class));
            assertEquals(0, new BigDecimal("9").compareTo(jdbc.queryForObject(
                    "SELECT maximum_lateral_weight_difference_percent FROM lp_policy", BigDecimal.class)));
            assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM fo_feed_receipt", Integer.class));
            assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM lp_feed_receipt", Integer.class));
            assertEquals(11, jdbc.queryForObject("SELECT flight_retention_days FROM oc_policy", Integer.class));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM oc_feed_receipt", Integer.class));
        }
    }
}
