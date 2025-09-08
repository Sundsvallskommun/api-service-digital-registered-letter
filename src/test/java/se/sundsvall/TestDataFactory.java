package se.sundsvall;

import java.time.OffsetDateTime;
import java.util.List;
import se.sundsvall.digitalregisteredletter.api.model.Attachments;
import se.sundsvall.digitalregisteredletter.api.model.AttachmentsBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterBuilder;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequestBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.model.LettersBuilder;
import se.sundsvall.digitalregisteredletter.api.model.SupportInfo;
import se.sundsvall.digitalregisteredletter.api.model.SupportInfoBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserV2;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.PartsResponsiveBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterHiddenBuilder;

public class TestDataFactory {

	public static final OffsetDateTime NOW = OffsetDateTime.now();

	public static LetterEntity createLetterEntity() {
		return LetterEntity.create()
			.withId("letter-id")
			.withMunicipalityId("municipality-id")
			.withBody("This is the body of the letter")
			.withContentType("text/plain")
			.withStatus("NEW")
			.withAttachments(List.of(createAttachmentEntity()))
			.withSupportInfo(createLetterEntitySupportInfo())
			.withCreated(NOW)
			.withUpdated(NOW);
	}

	public static LetterRequest createLetterRequest() {
		return LetterRequestBuilder.create()
			.withBody("This is the body of the letter")
			.withSubject("This is the subject")
			.withContentType("text/plain")
			.withPartyId("ce408061-9e38-4fca-a3e1-220b06f7bd23")
			.withSupportInfo(createSupportInfo())
			.build();
	}

	public static Attachments createAttachments() {
		return AttachmentsBuilder.create()
			.withFiles(List.of())
			.build();
	}

	public static Letter createLetter() {
		return LetterBuilder.create()
			.build();
	}

	public static Letters createLetters() {
		return LettersBuilder.create()
			.build();
	}

	public static se.sundsvall.digitalregisteredletter.integration.db.model.SupportInfo createLetterEntitySupportInfo() {
		return se.sundsvall.digitalregisteredletter.integration.db.model.SupportInfo.create()
			.withSupportText("Support text")
			.withContactInformationUrl("https://example.com/support")
			.withContactInformationEmail("support@email.com")
			.withContactInformationPhoneNumber("+46123456789");
	}

	public static SupportInfo createSupportInfo() {
		return SupportInfoBuilder.create()
			.withSupportText("Support text")
			.withContactInformationUrl("https://example.com/support")
			.withContactInformationEmail("support@email.com")
			.withContactInformationPhoneNumber("+46123456789")
			.build();
	}

	public static AttachmentEntity createAttachmentEntity() {
		return AttachmentEntity.create()
			.withId("attachment-id")
			.withFileName("file.txt")
			.withContentType("text/plain")
			.withContent(null);
	}

	public static ContentUserV2.PartsResponsive createPartsResponsive() {
		return PartsResponsiveBuilder.create()
			.withData("data")
			.withName("name")
			.withContentType("text/plain")
			.build();
	}

	public static ContentUserV2.RegisteredLetter createRegisteredLetter() {
		return RegisteredLetterBuilder.create()
			.withSenderReference(new ContentUserV2.RegisteredLetter.SenderReference("senderReference"))
			.withExpiresAt(OffsetDateTime.now())
			.withHidden(createRegisteredLetterHidden())
			.build();
	}

	public static ContentUserV2.RegisteredLetter.RegisteredLetterHidden createRegisteredLetterHidden() {
		return RegisteredLetterHiddenBuilder.create()
			.withSender(false)
			.withSubject(false)
			.build();
	}
}
