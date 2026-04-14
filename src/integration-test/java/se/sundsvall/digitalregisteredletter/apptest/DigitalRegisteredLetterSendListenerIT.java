package se.sundsvall.digitalregisteredletter.apptest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.digitalregisteredletter.integration.rabbitmq.configuration.Constants.POST_PORTAL_SERVICE_EXCHANGE;
import static se.sundsvall.digitalregisteredletter.integration.rabbitmq.configuration.Constants.SEND_DIGITAL_REGISTERED_LETTER_QUEUE;
import static se.sundsvall.digitalregisteredletter.integration.rabbitmq.configuration.Constants.STATUS_DIGITAL_REGISTERED_LETTER_QUEUE;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.model.DigitalRegisteredLetterSendEvent;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.model.DigitalRegisteredLetterStatusEvent;
import se.sundsvall.digitalregisteredletter.service.LetterService;

@WireMockAppTestSuite(files = "classpath:/SendListenerIT/", classes = Application.class)
@ContextConfiguration(initializers = RabbitMQContainerInitializer.class)
class DigitalRegisteredLetterSendListenerIT extends AbstractAppTest {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@MockitoBean
	private LetterService letterService;

	private DigitalRegisteredLetterSendEvent createFatEvent() {
		var sender = new DigitalRegisteredLetterSendEvent.Sender(
			"testuser; type=adAccount", "5566778899", "Test Organization",
			"Support text", "https://example.com",
			"test@example.com", "0701234567");
		var recipient = new DigitalRegisteredLetterSendEvent.Recipient("6a5c3d04-412d-11ec-973a-0242ac130003");
		var message = new DigitalRegisteredLetterSendEvent.Message("Test subject", "Test body", "text/plain", List.of("att-1"));
		return new DigitalRegisteredLetterSendEvent("2281", "test-request-id", "recipient-1", sender, recipient, message);
	}

	@Test
	void test01_successfulProcessing() {
		setupCall();

		when(letterService.sendLetter(anyString(), anyString(), any(), any()))
			.thenReturn(new Letter(null, null, null, null, null, null, null, null, null, null));

		var event = createFatEvent();

		rabbitTemplate.convertAndSend(POST_PORTAL_SERVICE_EXCHANGE, SEND_DIGITAL_REGISTERED_LETTER_QUEUE, event);

		await().atMost(60, SECONDS).untilAsserted(() ->
			verify(letterService).sendLetter(eq("2281"), eq("5566778899"), any(), any()));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var statusMessage = rabbitTemplate.receiveAndConvert(STATUS_DIGITAL_REGISTERED_LETTER_QUEUE);
			assertThat(statusMessage).isNotNull().isInstanceOf(DigitalRegisteredLetterStatusEvent.class);
			var statusEvent = (DigitalRegisteredLetterStatusEvent) statusMessage;
			assertThat(statusEvent.recipientId()).isEqualTo("recipient-1");
			assertThat(statusEvent.status()).isEqualTo("SENT");
		});

		verifyStubs();
	}

	@Test
	void test02_retriesAndSendsToStatusQueueOnPermanentFailure() {
		setupCall();

		var event = createFatEvent();

		rabbitTemplate.convertAndSend(POST_PORTAL_SERVICE_EXCHANGE, SEND_DIGITAL_REGISTERED_LETTER_QUEUE, event);

		await().atMost(20, SECONDS).untilAsserted(() -> {
			var statusMessage = rabbitTemplate.receiveAndConvert(STATUS_DIGITAL_REGISTERED_LETTER_QUEUE);
			assertThat(statusMessage)
				.isNotNull()
				.isInstanceOf(DigitalRegisteredLetterStatusEvent.class);

			var statusEvent = (DigitalRegisteredLetterStatusEvent) statusMessage;
			assertThat(statusEvent.recipientId()).isEqualTo("recipient-1");
			assertThat(statusEvent.status()).isEqualTo("FAILED");
		});

		verify(letterService, never()).sendLetter(anyString(), any(), any(), any());

		verifyStubs();
	}
}
