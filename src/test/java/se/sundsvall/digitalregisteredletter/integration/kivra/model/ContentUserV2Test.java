package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentUserV2Test {

	private static final String LEGAL_ID = "1234567890";
	private static final String SUBJECT = "subject";
	private static final String TYPE = "type";
	private static final String PARTS_RESPONSIVE_NAME = "partsResponsiveName";
	private static final String PARTS_RESPONSIVE_DATA = "partsResponsiveData";
	private static final String PARTS_RESPONSIVE_CONTENT_TYPE = "partsResponsiveContentType";
	private static final OffsetDateTime REGISTERED_LETTER_EXPIRES_AT = OffsetDateTime.now();
	private static final String REGISTERED_LETTER_SENDER_REFERENCE = "registeredLetterSenderReference";
	private static final Boolean REGISTERED_LETTER_HIDDEN_SENDER = false;
	private static final Boolean REGISTERED_LETTER_HIDDEN_SUBJECT = false;

	@Test
	void testConstructor() {
		var partsResponsive = new ContentUserV2.PartsResponsive(PARTS_RESPONSIVE_NAME, PARTS_RESPONSIVE_DATA, PARTS_RESPONSIVE_CONTENT_TYPE);
		var registeredLetterHidden = new ContentUserV2.RegisteredLetter.RegisteredLetterHidden(REGISTERED_LETTER_HIDDEN_SENDER, REGISTERED_LETTER_HIDDEN_SUBJECT);
		var senderReference = new ContentUserV2.RegisteredLetter.SenderReference(REGISTERED_LETTER_SENDER_REFERENCE);
		var registeredLetter = new ContentUserV2.RegisteredLetter(REGISTERED_LETTER_EXPIRES_AT, senderReference, registeredLetterHidden);

		var contentUser = new ContentUserV2(LEGAL_ID, SUBJECT, TYPE, registeredLetter, List.of(partsResponsive));

		assertThat(contentUser.legalId()).isEqualTo(LEGAL_ID);
		assertThat(contentUser.subject()).isEqualTo(SUBJECT);
		assertThat(contentUser.type()).isEqualTo(TYPE);
		assertThat(contentUser.registered()).isEqualTo(registeredLetter);
		assertThat(contentUser.registered()).satisfies(registered -> {
			assertThat(registered.expiresAt()).isEqualTo(REGISTERED_LETTER_EXPIRES_AT);
			assertThat(registered.senderReference()).isEqualTo(new ContentUserV2.RegisteredLetter.SenderReference(REGISTERED_LETTER_SENDER_REFERENCE));
			assertThat(registered.hidden()).satisfies(hidden -> {
				assertThat(hidden.sender()).isEqualTo(REGISTERED_LETTER_HIDDEN_SENDER);
				assertThat(hidden.subject()).isEqualTo(REGISTERED_LETTER_HIDDEN_SUBJECT);
			});
		});
		assertThat(contentUser.parts()).containsExactly(partsResponsive);
		assertThat(contentUser.parts()).allSatisfy(part -> {
			assertThat(part.name()).isEqualTo(PARTS_RESPONSIVE_NAME);
			assertThat(part.data()).isEqualTo(PARTS_RESPONSIVE_DATA);
			assertThat(part.contentType()).isEqualTo(PARTS_RESPONSIVE_CONTENT_TYPE);
		});
	}

	@Test
	void testBuilder() {
		var senderReference = new ContentUserV2.RegisteredLetter.SenderReference(REGISTERED_LETTER_SENDER_REFERENCE);
		var partsResponsive = PartsResponsiveBuilder.create()
			.withData(PARTS_RESPONSIVE_DATA)
			.withName(PARTS_RESPONSIVE_NAME)
			.withContentType(PARTS_RESPONSIVE_CONTENT_TYPE)
			.build();

		var registeredLetterHidden = RegisteredLetterHiddenBuilder.create()
			.withSender(REGISTERED_LETTER_HIDDEN_SENDER)
			.withSubject(REGISTERED_LETTER_HIDDEN_SUBJECT)
			.build();

		var registeredLetter = RegisteredLetterBuilder.create()
			.withExpiresAt(REGISTERED_LETTER_EXPIRES_AT)
			.withSenderReference(senderReference)
			.withHidden(registeredLetterHidden)
			.build();

		var contentUser = ContentUserV2Builder.create()
			.withLegalId(LEGAL_ID)
			.withSubject(SUBJECT)
			.withType(TYPE)
			.withRegistered(registeredLetter)
			.withParts(List.of(partsResponsive))
			.build();

		assertThat(contentUser.legalId()).isEqualTo(LEGAL_ID);
		assertThat(contentUser.subject()).isEqualTo(SUBJECT);
		assertThat(contentUser.type()).isEqualTo(TYPE);
		assertThat(contentUser.registered()).isEqualTo(registeredLetter);
		assertThat(contentUser.registered()).satisfies(registered -> {
			assertThat(registered.expiresAt()).isEqualTo(REGISTERED_LETTER_EXPIRES_AT);
			assertThat(registered.senderReference()).isEqualTo(new ContentUserV2.RegisteredLetter.SenderReference(REGISTERED_LETTER_SENDER_REFERENCE));
			assertThat(registered.hidden()).satisfies(hidden -> {
				assertThat(hidden.sender()).isEqualTo(REGISTERED_LETTER_HIDDEN_SENDER);
				assertThat(hidden.subject()).isEqualTo(REGISTERED_LETTER_HIDDEN_SUBJECT);
			});
		});
		assertThat(contentUser.parts()).containsExactly(partsResponsive);
		assertThat(contentUser.parts()).allSatisfy(part -> {
			assertThat(part.name()).isEqualTo(PARTS_RESPONSIVE_NAME);
			assertThat(part.data()).isEqualTo(PARTS_RESPONSIVE_DATA);
			assertThat(part.contentType()).isEqualTo(PARTS_RESPONSIVE_CONTENT_TYPE);
		});
	}
}
