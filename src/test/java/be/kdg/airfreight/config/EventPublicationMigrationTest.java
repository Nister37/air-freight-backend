package be.kdg.airfreight.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventPublicationMigrationTest {
    @Test
    void startupRetargetsPendingPublicationsWithoutChangingTheirPayloadOrOtherListeners() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL", "sa", "")) {
            var dataSource = new SingleConnectionDataSource(connection, true);
            var jdbc = new JdbcTemplate(dataSource);
            var schema = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
            schema.execute(dataSource);

            String eventType = "be.kdg.airfreight.partnermessaging.events.PartnerMessageCreatedEvent";
            String oldId = "be.kdg.airfreight.partnermessaging.adapter.out.rabbit.RabbitPartnerMessageAdapter.publish("
                    + eventType + ")";
            String defaultId = "be.kdg.airfreight.partnermessaging.adapter.out.rabbit.PartnerMessageRabbitPublisherAdapter.publish("
                    + eventType + ")";
            UUID pending = UUID.randomUUID();
            UUID completed = UUID.randomUUID();
            UUID unrelated = UUID.randomUUID();
            for (var entry : Map.of(pending, oldId, completed, oldId, unrelated, "another.listener").entrySet()) {
                jdbc.update("""
                        INSERT INTO event_publication
                        (id, listener_id, event_type, serialized_event, publication_date, status, completion_attempts)
                        VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, 'FAILED', 2)
                        """, entry.getKey(), entry.getValue(), eventType, "{\"message\":\"preserve this payload\"}");
            }
            jdbc.update("UPDATE event_publication SET completion_date = CURRENT_TIMESTAMP, status = 'COMPLETED' WHERE id = ?",
                    completed);
            var pendingBefore = jdbc.queryForMap("SELECT * FROM event_publication WHERE id = ?", pending);
            var completedBefore = jdbc.queryForMap("SELECT * FROM event_publication WHERE id = ?", completed);
            var unrelatedBefore = jdbc.queryForMap("SELECT * FROM event_publication WHERE id = ?", unrelated);

            schema.execute(dataSource);
            schema.execute(dataSource);

            pendingBefore.put("LISTENER_ID", defaultId);
            assertEquals(pendingBefore, jdbc.queryForMap("SELECT * FROM event_publication WHERE id = ?", pending));
            assertEquals(completedBefore, jdbc.queryForMap("SELECT * FROM event_publication WHERE id = ?", completed));
            assertEquals(unrelatedBefore, jdbc.queryForMap("SELECT * FROM event_publication WHERE id = ?", unrelated));
            assertEquals(3, jdbc.queryForObject("SELECT COUNT(*) FROM event_publication", Integer.class));
        }
    }
}
