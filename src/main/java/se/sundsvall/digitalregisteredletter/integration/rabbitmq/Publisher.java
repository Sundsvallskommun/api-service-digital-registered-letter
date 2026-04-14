package se.sundsvall.digitalregisteredletter.integration.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.configuration.Queue;

@Component
public class Publisher {

	private final RabbitTemplate rabbitTemplate;

	public Publisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publishEvent(final Queue queue, final Object event) {
		rabbitTemplate.convertAndSend(queue.getExchange(), queue.getRoutingKey(), event);
	}

}
