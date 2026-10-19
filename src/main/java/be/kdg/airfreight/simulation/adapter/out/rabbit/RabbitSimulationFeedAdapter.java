package be.kdg.airfreight.simulation.adapter.out.rabbit;

import be.kdg.airfreight.common.CargoBooking;
import be.kdg.airfreight.common.FlightSchedule;
import be.kdg.airfreight.simulation.port.out.SimulationFeedPort;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.*;
import java.util.Set;
import java.util.UUID;

@Component
@Profile("simulation & !prod & !production")
class RabbitSimulationFeedAdapter implements SimulationFeedPort {
    private final RabbitTemplate rabbit;

    RabbitSimulationFeedAdapter(@Qualifier("rabbitTemplate") RabbitTemplate rabbit) { this.rabbit = rabbit; }

    @Override
    public void publishSchedule(UUID messageId, FlightSchedule schedule) {
        publish("ssm", messageId, new SsmMessage(messageId, schedule.from(), schedule.to(), schedule.weekdays(),
                schedule.departureTime(), schedule.arrivalTime(), schedule.flightNumber().value(),
                schedule.departureAirport().value(), schedule.arrivalAirport().value(),
                schedule.aircraftRegistration().value()));
    }

    @Override
    public void publishCargo(CargoBooking booking) {
        publish("uld", booking.messageId(), new UldMessage(booking.messageId(), booking.flightId().flightNumber().value(),
                booking.flightId().scheduledDepartureDate().getDayOfMonth(), booking.flightId().departureAirport().value(),
                booking.uldCode(), booking.weight(), booking.unit(), booking.destination().value()));
    }

    private void publish(String routingKey, UUID messageId, Object payload) {
        rabbit.convertAndSend("airfreight.feeds", routingKey, payload, message -> {
            message.getMessageProperties().setMessageId(messageId.toString());
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
    }

    // The external feed contract uses plain JSON fields, independently of receiver Java classes.
    private record SsmMessage(UUID messageId, LocalDate from, LocalDate to, Set<DayOfWeek> weekdays,
                              LocalTime departureTime, LocalTime arrivalTime, String flightNumber,
                              String departureAirport, String arrivalAirport, String aircraftRegistration) { }

    private record UldMessage(UUID messageId, String flightNumber, int departureDay,
                              String departureAirport, String uldCode, BigDecimal weight, String unit,
                              String destination) { }
}
