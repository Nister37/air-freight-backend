package be.kdg.airfreight.partnermessaging.adapter.out.persistence.repository;

import be.kdg.airfreight.partnermessaging.adapter.out.persistence.entity.PartnerMessageJpaEntity;
import be.kdg.airfreight.common.messages.MessageKind;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PartnerMessageJpaRepository extends JpaRepository<PartnerMessageJpaEntity, UUID> {
    boolean existsBySourceEventIdAndKind(UUID id, MessageKind kind);
    void deleteByFlightKey(String key);
}
