package be.kdg.airfreight.operationscoordination.adapter.in.listeners;

import be.kdg.airfreight.common.CargoBooking;
import be.kdg.airfreight.operationscoordination.port.in.command.ScheduleFeedCommand;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RouteCargoFeedUseCase;
import be.kdg.airfreight.operationscoordination.port.in.usecase.RouteScheduleFeedUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.core.JacksonException;
import org.springframework.amqp.core.*;
import tools.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeedListenerTest {
    private Message message(String json) {
        MessageProperties properties = new MessageProperties();
        properties.setMessageId("21731b72-6d23-4b58-aeba-05393f122341");
        properties.setContentType("application/json");
        return new Message(json.getBytes(StandardCharsets.UTF_8), properties);
    }
    @Test
    void mapsScheduleJsonToInclusiveUtcScheduleCommand() {
        RouteScheduleFeedUseCase coordinator = mock(RouteScheduleFeedUseCase.class);
        new ScheduleFeedListener(coordinator, new JsonMapper()).receive(message("""
                {"messageId":"21731b72-6d23-4b58-aeba-05393f122341",
                 "from":"2026-10-11","to":"2026-10-12","weekdays":["SUNDAY","MONDAY"],
                 "departureTime":"23:30:00","arrivalTime":"01:00:00",
                 "flightNumber":"HJ221","departureAirport":"BRU","arrivalAirport":"ATH","aircraftRegistration":"SXBAA"}
                """));
        ArgumentCaptor<ScheduleFeedCommand> command = ArgumentCaptor.forClass(ScheduleFeedCommand.class);
        verify(coordinator).routeScheduleFeed(command.capture());
        assertThat(command.getValue().schedule().operatingDates()).containsExactly(LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 12));
        assertThat(command.getValue().schedule().arrivalOn(LocalDate.of(2026, 10, 11)))
                .isEqualTo(LocalDateTime.of(2026, 10, 12, 1, 0));
    }
    @Test
    void malformedSchedulePropagatesToTheRabbitContainerWithoutApplyingAnything() {
        RouteScheduleFeedUseCase coordinator = mock(RouteScheduleFeedUseCase.class);
        ScheduleFeedListener listener = new ScheduleFeedListener(coordinator, new JsonMapper());
        assertThatThrownBy(() -> listener.receive(message("{broken"))).isInstanceOf(JacksonException.class);
        verifyNoInteractions(coordinator);
    }
    @Test
    void mapsCargoFeedToLoadIntegrationContractWithoutImplementingLoadRules() {
        RouteCargoFeedUseCase coordinator = mock(RouteCargoFeedUseCase.class);
        new CargoFeedListener(coordinator, new JsonMapper()).receive(message("""
                {"messageId":"21731b72-6d23-4b58-aeba-05393f122341","flightNumber":"HJ221",
                 "scheduledDepartureDate":"2026-10-11","departureAirport":"BRU","uldCode":"AKE16689R7",
                 "weight":1000,"unit":"KG","destination":"ATH"}
                """));
        ArgumentCaptor<CargoBooking> booking = ArgumentCaptor.forClass(CargoBooking.class);
        verify(coordinator).routeCargoFeed(booking.capture());
        assertThat(booking.getValue().flightId().key()).isEqualTo("HJ221/2026-10-11/BRU");
        assertThat(booking.getValue().uldCode()).isEqualTo("AKE16689R7");
    }

    @Test
    void businessRefusalPropagatesUnchangedToTheRabbitContainer() {
        RouteScheduleFeedUseCase coordinator = mock(RouteScheduleFeedUseCase.class);
        IllegalArgumentException refusal = new IllegalArgumentException("Schedule cannot be applied");
        doThrow(refusal).when(coordinator).routeScheduleFeed(any());
        ScheduleFeedListener listener = new ScheduleFeedListener(coordinator, new JsonMapper());
        assertThatThrownBy(() -> listener.receive(message("""
                {"messageId":"21731b72-6d23-4b58-aeba-05393f122341",
                 "from":"2026-10-11","to":"2026-10-12","weekdays":["SUNDAY","MONDAY"],
                 "departureTime":"23:30:00","arrivalTime":"01:00:00",
                 "flightNumber":"HJ221","departureAirport":"BRU","arrivalAirport":"ATH","aircraftRegistration":"SXBAA"}
                """))).isSameAs(refusal);
    }
}
