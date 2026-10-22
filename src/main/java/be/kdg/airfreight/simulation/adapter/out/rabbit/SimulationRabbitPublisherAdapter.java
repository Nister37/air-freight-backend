package be.kdg.airfreight.simulation.adapter.out.rabbit;

import be.kdg.airfreight.common.CargoBooking;
import be.kdg.airfreight.common.FlightSchedule;
import be.kdg.airfreight.common.messages.SsmMessage;
import be.kdg.airfreight.common.messages.UldMessage;
import be.kdg.airfreight.simulation.port.out.SimulationMessagePublisherPort;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("simulation & !prod & !production")
class SimulationRabbitPublisherAdapter implements SimulationMessagePublisherPort {
    private final RabbitTemplate rabbit;

    SimulationRabbitPublisherAdapter(RabbitTemplate rabbitTemplate) { this.rabbit = rabbitTemplate; }

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

}
