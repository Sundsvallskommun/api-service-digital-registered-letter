package se.sundsvall.digitalregisteredletter.integration.rabbitmq;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.configuration.Queue;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.model.DigitalRegisteredLetterStatusEvent;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class PublisherTest {

	@Mock
	private RabbitTemplate rabbitTemplateMock;

	@InjectMocks
	private Publisher publisher;

	@AfterEach
	void verifyNoMoreMockInteractions() {
		verifyNoMoreInteractions(rabbitTemplateMock);
	}

	@Test
	void publishEvent() {
		var queue = Queue.STATUS_DIGITAL_REGISTERED_LETTER;
		var event = new DigitalRegisteredLetterStatusEvent("recipientId", "externalId", "SENT", null);

		publisher.publishEvent(queue, event);

		verify(rabbitTemplateMock).convertAndSend(queue.getExchange(), queue.getRoutingKey(), event);
	}
}
