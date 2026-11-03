package be.kdg.airfreight.simulation.adapter.out.rabbit;

import be.kdg.airfreight.simulation.port.in.command.SimulateCargoBookingCommand;
import be.kdg.airfreight.simulation.port.in.command.SimulateScheduleCommand;
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
    public void publishSchedule(UUID messageId, SimulateScheduleCommand command) {
        publish("ssm", messageId, new SsmMessage(messageId, command.from(), command.to(), command.weekdays(),
                command.departureTime(), command.arrivalTime(), command.flightNumber().value(),
                command.departureAirport().value(), command.arrivalAirport().value(),
                command.aircraftRegistration().value()));
    }

    @Override
    public void publishCargo(SimulateCargoBookingCommand command) {
        publish("uld", command.messageId(), new UldMessage(command.messageId(), command.flightId().flightNumber().value(),
                command.flightId().scheduledDepartureDate().getDayOfMonth(), command.flightId().departureAirport().value(),
                command.uldCode(), command.weight(), command.unit(), command.destination().value()));
    }

    private void publish(String routingKey, UUID messageId, Object payload) {
        rabbit.convertAndSend("airfreight.feeds", routingKey, payload, message -> {
            message.getMessageProperties().setMessageId(messageId.toString());
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
    }

}
