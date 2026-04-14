package se.sundsvall.digitalregisteredletter.integration.rabbitmq;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.integration.postportalservice.PostportalserviceIntegration;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.configuration.Queue;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.model.DigitalRegisteredLetterSendEvent;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.model.DigitalRegisteredLetterStatusEvent;
import se.sundsvall.digitalregisteredletter.service.LetterService;
import se.sundsvall.digitalregisteredletter.service.model.AttachmentData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DigitalRegisteredLetterSendListenerTest {

	@Mock
	private PostportalserviceIntegration postportalserviceIntegrationMock;

	@Mock
	private LetterService letterServiceMock;

	@Mock
	private Publisher publisherMock;

	@Mock
	private Dept44HealthUtility dept44HealthUtilityMock;

	@InjectMocks
	private DigitalRegisteredLetterSendListener digitalRegisteredLetterSendListener;

	@Captor
	private ArgumentCaptor<DigitalRegisteredLetterStatusEvent> statusEventCaptor;

	@AfterEach
	void verifyNoMoreMockInteractions() {
		verifyNoMoreInteractions(postportalserviceIntegrationMock, letterServiceMock, publisherMock, dept44HealthUtilityMock);
	}

	@Test
	void handleEvent_successfulFlow() throws IOException {
		// Arrange
		var sender = new DigitalRegisteredLetterSendEvent.Sender(
			"identifier-value", "123456", "OrgName", "supportText",
			"https://url.com", "email@example.com", "+46701234567");
		var recipient = new DigitalRegisteredLetterSendEvent.Recipient("partyId-123");
		var message = new DigitalRegisteredLetterSendEvent.Message("subject", "body", "text/plain", List.of("att-1", "att-2"));
		var event = new DigitalRegisteredLetterSendEvent("municipalityId", "requestId-123", "recipientId", sender, recipient, message);

		var attachmentData1 = mock(AttachmentData.class);
		var attachmentData2 = mock(AttachmentData.class);
		var attachments = List.of(attachmentData1, attachmentData2);
		var letterRequest = digitalRegisteredLetterSendListener.toLetterRequest(event);
		var letter = mock(Letter.class);

		when(postportalserviceIntegrationMock.getAttachment("municipalityId", "att-1")).thenReturn(attachmentData1);
		when(postportalserviceIntegrationMock.getAttachment("municipalityId", "att-2")).thenReturn(attachmentData2);
		when(letterServiceMock.sendLetter("municipalityId", "123456", letterRequest, attachments)).thenReturn(letter);
		when(letter.id()).thenReturn("letter-id-123");

		// Act
		digitalRegisteredLetterSendListener.handleEvent(event);

		// Assert
		verify(postportalserviceIntegrationMock).getAttachment("municipalityId", "att-1");
		verify(postportalserviceIntegrationMock).getAttachment("municipalityId", "att-2");
		verify(letterServiceMock).sendLetter("municipalityId", "123456", letterRequest, attachments);
		verify(publisherMock).publishEvent(eq(Queue.STATUS_DIGITAL_REGISTERED_LETTER), statusEventCaptor.capture());

		var statusEvent = statusEventCaptor.getValue();
		assertThat(statusEvent.recipientId()).isEqualTo("recipientId");
		assertThat(statusEvent.externalId()).isEqualTo("letter-id-123");
		assertThat(statusEvent.status()).isEqualTo("SENT");
		assertThat(statusEvent.statusDetail()).isNull();
	}

	@Test
	void handleEvent_singleAttachment() throws IOException {
		// Arrange
		var sender = new DigitalRegisteredLetterSendEvent.Sender(
			"identifier-value", "123456", "OrgName", "supportText",
			"https://url.com", "email@example.com", "+46701234567");
		var recipient = new DigitalRegisteredLetterSendEvent.Recipient("partyId-123");
		var message = new DigitalRegisteredLetterSendEvent.Message("subject", "body", "text/plain", List.of("att-1"));
		var event = new DigitalRegisteredLetterSendEvent("municipalityId", "requestId-456", "recipientId", sender, recipient, message);

		var attachmentData = mock(AttachmentData.class);
		var attachments = List.of(attachmentData);
		var letterRequest = digitalRegisteredLetterSendListener.toLetterRequest(event);
		var letter = mock(Letter.class);

		when(postportalserviceIntegrationMock.getAttachment("municipalityId", "att-1")).thenReturn(attachmentData);
		when(letterServiceMock.sendLetter("municipalityId", "123456", letterRequest, attachments)).thenReturn(letter);
		when(letter.id()).thenReturn("letter-id-456");

		// Act
		digitalRegisteredLetterSendListener.handleEvent(event);

		// Assert
		verify(postportalserviceIntegrationMock).getAttachment("municipalityId", "att-1");
		verify(letterServiceMock).sendLetter("municipalityId", "123456", letterRequest, attachments);
		verify(publisherMock).publishEvent(eq(Queue.STATUS_DIGITAL_REGISTERED_LETTER), statusEventCaptor.capture());

		var statusEvent = statusEventCaptor.getValue();
		assertThat(statusEvent.status()).isEqualTo("SENT");
	}

	@Test
	void toLetterRequest_withAllFieldsPopulated() {
		var sender = new DigitalRegisteredLetterSendEvent.Sender(
			"identifier", "123456", "OrgName", "supportText",
			"https://url.com", "email@example.com", "+46701234567");
		var recipient = new DigitalRegisteredLetterSendEvent.Recipient("partyId-123");
		var message = new DigitalRegisteredLetterSendEvent.Message("subject", "body", "text/plain", List.of("att-1"));
		var event = new DigitalRegisteredLetterSendEvent("municipalityId", "requestId", "recipientId", sender, recipient, message);

		var result = digitalRegisteredLetterSendListener.toLetterRequest(event);

		assertThat(result.partyId()).isEqualTo("partyId-123");
		assertThat(result.subject()).isEqualTo("subject");
		assertThat(result.body()).isEqualTo("body");
		assertThat(result.contentType()).isEqualTo("text/plain");
		assertThat(result.supportInfo().supportText()).isEqualTo("supportText");
		assertThat(result.supportInfo().contactInformationUrl()).isEqualTo("https://url.com");
		assertThat(result.supportInfo().contactInformationPhoneNumber()).isEqualTo("+46701234567");
		assertThat(result.supportInfo().contactInformationEmail()).isEqualTo("email@example.com");
		assertThat(result.organization().number()).isEqualTo(123456L);
		assertThat(result.organization().name()).isEqualTo("OrgName");
	}

	@Test
	void toLetterRequest_organizationNumberParsedToLong() {
		var sender = new DigitalRegisteredLetterSendEvent.Sender(
			"identifier", "999999", "OrgName", "supportText",
			"https://url.com", "email@example.com", "+46701234567");
		var recipient = new DigitalRegisteredLetterSendEvent.Recipient("partyId-123");
		var message = new DigitalRegisteredLetterSendEvent.Message("subject", "body", "text/plain", List.of());
		var event = new DigitalRegisteredLetterSendEvent("municipalityId", "requestId", "recipientId", sender, recipient, message);

		var result = digitalRegisteredLetterSendListener.toLetterRequest(event);

		assertThat(result.organization().number()).isEqualTo(999999L);
	}

	@Test
	void toLetterRequest_nullOrganizationNumber() {
		var sender = new DigitalRegisteredLetterSendEvent.Sender(
			"identifier", null, "OrgName", "supportText",
			"https://url.com", "email@example.com", "+46701234567");
		var recipient = new DigitalRegisteredLetterSendEvent.Recipient("partyId-123");
		var message = new DigitalRegisteredLetterSendEvent.Message("subject", "body", "text/plain", List.of());
		var event = new DigitalRegisteredLetterSendEvent("municipalityId", "requestId", "recipientId", sender, recipient, message);

		var result = digitalRegisteredLetterSendListener.toLetterRequest(event);

		assertThat(result.organization().number()).isNull();
	}

	@Test
	void handleEvent_publisherThrowsException_doesNotPropagateError() throws IOException {
		// Arrange
		var sender = new DigitalRegisteredLetterSendEvent.Sender(
			"identifier-value", "123456", "OrgName", "supportText",
			"https://url.com", "email@example.com", "+46701234567");
		var recipient = new DigitalRegisteredLetterSendEvent.Recipient("partyId-123");
		var message = new DigitalRegisteredLetterSendEvent.Message("subject", "body", "text/plain", List.of("att-1"));
		var event = new DigitalRegisteredLetterSendEvent("municipalityId", "requestId-789", "recipientId", sender, recipient, message);

		var attachmentData = mock(AttachmentData.class);
		var letterRequest = digitalRegisteredLetterSendListener.toLetterRequest(event);
		var letter = mock(Letter.class);

		when(postportalserviceIntegrationMock.getAttachment("municipalityId", "att-1")).thenReturn(attachmentData);
		when(letterServiceMock.sendLetter("municipalityId", "123456", letterRequest, List.of(attachmentData))).thenReturn(letter);
		when(letter.id()).thenReturn("letter-id-789");
		doThrow(new RuntimeException("RabbitMQ connection lost")).when(publisherMock).publishEvent(any(), any());

		// Act - should NOT throw despite publisher failure
		digitalRegisteredLetterSendListener.handleEvent(event);

		// Assert - letter was sent, publisher was called, and health was set to unhealthy
		verify(postportalserviceIntegrationMock).getAttachment("municipalityId", "att-1");
		verify(letterServiceMock).sendLetter("municipalityId", "123456", letterRequest, List.of(attachmentData));
		verify(publisherMock).publishEvent(eq(Queue.STATUS_DIGITAL_REGISTERED_LETTER), any(DigitalRegisteredLetterStatusEvent.class));
		verify(dept44HealthUtilityMock).setHealthIndicatorUnhealthy(eq("handleEvent"), any(String.class));
	}

	@Test
	void handleEvent_hasTransactionalAnnotation() throws NoSuchMethodException {
		var method = DigitalRegisteredLetterSendListener.class.getMethod("handleEvent", DigitalRegisteredLetterSendEvent.class);
		var transactional = method.getAnnotation(Transactional.class);

		assertThat(transactional).isNotNull();
	}
}
