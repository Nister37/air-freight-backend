package be.kdg.airfreight.operationscoordination.adapter.in.listeners;

import be.kdg.airfreight.common.CargoBooking;
import be.kdg.airfreight.operationscoordination.adapter.config.FeedRabbitTopology;
import be.kdg.airfreight.operationscoordination.port.in.command.ScheduleFeedCommand;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RouteCargoFeedUseCase;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RouteScheduleFeedUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class FeedListenerTest {
    @Autowired RabbitListenerEndpointRegistry listeners;
    @MockitoBean RouteScheduleFeedUseCase schedules;
    @MockitoBean RouteCargoFeedUseCase cargo;

    private void deliver(String queue, String json) throws Exception {
        ChannelAwareMessageListener listener = listeners.getListenerContainers().stream()
                .map(AbstractMessageListenerContainer.class::cast)
                .filter(container -> Arrays.asList(container.getQueueNames()).contains(queue))
                .map(container -> (ChannelAwareMessageListener) container.getMessageListener())
                .findFirst().orElseThrow();
        // Exercise the actual configured AMQP conversion and method dispatch, without a broker.
        listener.onMessage(message(json), null);
    }

    private Message message(String json) {
        MessageProperties properties = new MessageProperties();
        properties.setMessageId("21731b72-6d23-4b58-aeba-05393f122341");
        properties.setContentType("application/json");
        return new Message(json.getBytes(StandardCharsets.UTF_8), properties);
    }
    @Test
    void mapsScheduleJsonToInclusiveUtcScheduleCommand() throws Exception {
        deliver(FeedRabbitTopology.SSM_QUEUE, """
                {"messageId":"21731b72-6d23-4b58-aeba-05393f122341",
                 "from":"2026-10-11","to":"2026-10-12","weekdays":["SUNDAY","MONDAY"],
                 "departureTime":"23:30:00","arrivalTime":"01:00:00",
                 "flightNumber":"HJ221","departureAirport":"BRU","arrivalAirport":"ATH","aircraftRegistration":"SXBAA"}
                """);
        ArgumentCaptor<ScheduleFeedCommand> command = ArgumentCaptor.forClass(ScheduleFeedCommand.class);
        verify(schedules).routeScheduleFeed(command.capture());
        assertThat(command.getValue().schedule().operatingDates()).containsExactly(LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 12));
        assertThat(command.getValue().schedule().arrivalOn(LocalDate.of(2026, 10, 11)))
                .isEqualTo(LocalDateTime.of(2026, 10, 12, 1, 0));
    }
    @Test
    void malformedSchedulePropagatesToTheRabbitContainerWithoutApplyingAnything() {
        assertThatThrownBy(() -> deliver(FeedRabbitTopology.SSM_QUEUE, "{broken"))
                .hasCauseInstanceOf(MessageConversionException.class);
        verifyNoInteractions(schedules, cargo);
    }
    @Test
    void mapsCargoFeedToLoadIntegrationContractWithoutImplementingLoadRules() throws Exception {
        deliver(FeedRabbitTopology.ULD_QUEUE, """
                {"messageId":"21731b72-6d23-4b58-aeba-05393f122341","flightNumber":"HJ221",
                 "scheduledDepartureDate":"2026-10-11","departureAirport":"BRU","uldCode":"AKE16689R7",
                 "weight":1000,"unit":"KG","destination":"ATH"}
                """);
        ArgumentCaptor<CargoBooking> booking = ArgumentCaptor.forClass(CargoBooking.class);
        verify(cargo).routeCargoFeed(booking.capture());
        assertThat(booking.getValue().flightId().key()).isEqualTo("HJ221/2026-10-11/BRU");
        assertThat(booking.getValue().uldCode()).isEqualTo("AKE16689R7");
    }

    @Test
    void malformedCargoIsRejectedDuringConversionWithoutApplyingAnything() {
        assertThatThrownBy(() -> deliver(FeedRabbitTopology.ULD_QUEUE, "{broken"))
                .hasCauseInstanceOf(MessageConversionException.class);
        verifyNoInteractions(schedules, cargo);
    }

    @Test
    void businessRefusalPropagatesUnchangedToTheRabbitContainer() {
        IllegalArgumentException refusal = new IllegalArgumentException("Schedule cannot be applied");
        doThrow(refusal).when(schedules).routeScheduleFeed(any());
        assertThatThrownBy(() -> deliver(FeedRabbitTopology.SSM_QUEUE, """
                {"messageId":"21731b72-6d23-4b58-aeba-05393f122341",
                 "from":"2026-10-11","to":"2026-10-12","weekdays":["SUNDAY","MONDAY"],
                 "departureTime":"23:30:00","arrivalTime":"01:00:00",
                 "flightNumber":"HJ221","departureAirport":"BRU","arrivalAirport":"ATH","aircraftRegistration":"SXBAA"}
                """)).hasCause(refusal);
    }
}
