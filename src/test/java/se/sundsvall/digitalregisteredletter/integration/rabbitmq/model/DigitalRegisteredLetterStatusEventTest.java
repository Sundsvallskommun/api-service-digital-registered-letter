package se.sundsvall.digitalregisteredletter.integration.rabbitmq.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DigitalRegisteredLetterStatusEventTest {

	@Test
	void testAllFields() {
		var event = new DigitalRegisteredLetterStatusEvent("recipientId", "externalId", "SENT", "detail");

		assertThat(event.recipientId()).isEqualTo("recipientId");
		assertThat(event.externalId()).isEqualTo("externalId");
		assertThat(event.status()).isEqualTo("SENT");
		assertThat(event.statusDetail()).isEqualTo("detail");
	}

	@Test
	void testNullFields() {
		var event = new DigitalRegisteredLetterStatusEvent(null, null, null, null);

		assertThat(event.recipientId()).isNull();
		assertThat(event.externalId()).isNull();
		assertThat(event.status()).isNull();
		assertThat(event.statusDetail()).isNull();
	}
}
