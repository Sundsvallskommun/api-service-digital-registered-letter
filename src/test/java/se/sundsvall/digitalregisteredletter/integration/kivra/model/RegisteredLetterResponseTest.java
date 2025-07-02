package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class RegisteredLetterResponseTest {

	private static final String STATUS = "status";
	private static final String INTERNAL_ID = "internalId";
	private static final OffsetDateTime SIGNED_AT = OffsetDateTime.now();

	@Test
	void constructorTest() {
		var senderReference = new RegisteredLetterResponse.SenderReference(INTERNAL_ID);
		var registeredLetterResponse = new RegisteredLetterResponse(STATUS, SIGNED_AT, senderReference);

		assertThat(registeredLetterResponse).isNotNull();
		assertThat(registeredLetterResponse.status()).isEqualTo(STATUS);
		assertThat(registeredLetterResponse.signedAt()).isEqualTo(SIGNED_AT);
		assertThat(registeredLetterResponse.senderReference()).isNotNull();
		assertThat(registeredLetterResponse.senderReference().internalId()).isEqualTo(INTERNAL_ID);
	}

	@Test
	void builderTest() {
		var senderReference = new RegisteredLetterResponse.SenderReference(INTERNAL_ID);
		var registeredLetterResponse = RegisteredLetterResponseBuilder.create()
			.withStatus(STATUS)
			.withSignedAt(SIGNED_AT)
			.withSenderReference(senderReference)
			.build();

		assertThat(registeredLetterResponse).isNotNull();
		assertThat(registeredLetterResponse.status()).isEqualTo(STATUS);
		assertThat(registeredLetterResponse.signedAt()).isEqualTo(SIGNED_AT);
		assertThat(registeredLetterResponse.senderReference()).isNotNull();
		assertThat(registeredLetterResponse.senderReference().internalId()).isEqualTo(INTERNAL_ID);
	}
}
