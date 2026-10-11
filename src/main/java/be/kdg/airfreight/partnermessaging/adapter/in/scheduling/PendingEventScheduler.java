package be.kdg.airfreight.partnermessaging.adapter.in.scheduling;

import org.springframework.modulith.events.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class PendingEventScheduler {
    private final FailedEventPublications publications;
    PendingEventScheduler(FailedEventPublications publications) { this.publications = publications; }
    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    void retryFailedEvents() { publications.resubmit(ResubmissionOptions.defaults()); }
}
