package se.sundsvall.digitalregisteredletter.integration.rabbitmq.model;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DigitalRegisteredLetterSendEventTest {

	@Test
	void testSenderRecord() {
		var sender = new DigitalRegisteredLetterSendEvent.Sender(
			"identifier", "123456", "OrgName", "supportText",
			"https://url.com", "email@example.com", "+46701234567");

		assertThat(sender.identifier()).isEqualTo("identifier");
		assertThat(sender.organizationNumber()).isEqualTo("123456");
		assertThat(sender.organizationName()).isEqualTo("OrgName");
		assertThat(sender.supportText()).isEqualTo("supportText");
		assertThat(sender.contactInformationUrl()).isEqualTo("https://url.com");
		assertThat(sender.contactInformationEmail()).isEqualTo("email@example.com");
		assertThat(sender.contactInformationPhoneNumber()).isEqualTo("+46701234567");
	}

	@Test
	void testRecipientRecord() {
		var recipient = new DigitalRegisteredLetterSendEvent.Recipient("partyId-123");

		assertThat(recipient.partyId()).isEqualTo("partyId-123");
	}

	@Test
	void testMessageRecord() {
		var attachmentIds = List.of("att-1", "att-2");
		var message = new DigitalRegisteredLetterSendEvent.Message("subject", "body", "text/plain", attachmentIds);

		assertThat(message.subject()).isEqualTo("subject");
		assertThat(message.body()).isEqualTo("body");
		assertThat(message.contentType()).isEqualTo("text/plain");
		assertThat(message.attachmentIds()).containsExactly("att-1", "att-2");
	}

	@Test
	void testSendRegisteredLetterEvent() {
		var sender = new DigitalRegisteredLetterSendEvent.Sender(
			"identifier", "123456", "OrgName", "supportText",
			"https://url.com", "email@example.com", "+46701234567");
		var recipient = new DigitalRegisteredLetterSendEvent.Recipient("partyId-123");
		var message = new DigitalRegisteredLetterSendEvent.Message("subject", "body", "text/plain", List.of("att-1"));

		var event = new DigitalRegisteredLetterSendEvent("municipalityId", "requestId", "recipientId", sender, recipient, message);

		assertThat(event.municipalityId()).isEqualTo("municipalityId");
		assertThat(event.requestId()).isEqualTo("requestId");
		assertThat(event.recipientId()).isEqualTo("recipientId");
		assertThat(event.sender()).isSameAs(sender);
		assertThat(event.recipient()).isSameAs(recipient);
		assertThat(event.message()).isSameAs(message);
	}

	@Test
	void testNullFields() {
		var event = new DigitalRegisteredLetterSendEvent(null, null, null, null, null, null);

		assertThat(event.municipalityId()).isNull();
		assertThat(event.requestId()).isNull();
		assertThat(event.recipientId()).isNull();
		assertThat(event.sender()).isNull();
		assertThat(event.recipient()).isNull();
		assertThat(event.message()).isNull();
	}
}
