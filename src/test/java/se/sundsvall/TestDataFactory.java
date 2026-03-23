package se.sundsvall;

import java.time.OffsetDateTime;
import java.util.List;
import se.sundsvall.digitalregisteredletter.api.model.DeviceBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterBuilder;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequestBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.model.LettersBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Organization;
import se.sundsvall.digitalregisteredletter.api.model.OrganizationBuilder;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfo;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfoBuilder;
import se.sundsvall.digitalregisteredletter.api.model.StepUpBuilder;
import se.sundsvall.digitalregisteredletter.api.model.SupportInfo;
import se.sundsvall.digitalregisteredletter.api.model.SupportInfoBuilder;
import se.sundsvall.digitalregisteredletter.api.model.UserBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.OrganizationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SupportInformation;

public class TestDataFactory {

	public static final OffsetDateTime NOW = OffsetDateTime.now();

	public static LetterEntity createLetterEntity() {
		final var letter = LetterEntity.create()
			.withId("letter-id")
			.withSubject("This is the subject of the letter")
			.withMunicipalityId("municipality-id")
			.withBody("This is the body of the letter")
			.withContentType("text/plain")
			.withStatus("NEW")
			.withAttachments(List.of(createAttachmentEntity()))
			.withSupportInformation(createSupportInformationEmbeddable())
			.withCreated(NOW.minusSeconds(30))
			.withUpdated(NOW);

		return letter.withOrganization(createOrganizationEntity(letter));
	}

	public static LetterRequest createLetterRequest() {
		return createLetterRequest(createOrganization());
	}

	public static LetterRequest createLetterRequest(Organization organization) {
		return LetterRequestBuilder.create()
			.withBody("This is the body of the letter")
			.withSubject("This is the subject")
			.withContentType("text/plain")
			.withPartyId("ce408061-9e38-4fca-a3e1-220b06f7bd23")
			.withSupportInfo(createSupportInfo())
			.withOrganization(organization)
			.build();
	}

	public static Letter createLetter() {
		return createLetter(null);
	}

	public static Letter createLetter(String id) {
		return LetterBuilder.create()
			.withId(id)
			.build();
	}

	public static Letters createLetters() {
		return LettersBuilder.create()
			.build();
	}

	public static SigningInfo createSigningInfo() {
		return SigningInfoBuilder.create()
			.withContentKey("contentKey")
			.withDevice(DeviceBuilder.create()
				.withIpAddress("ipAddress")
				.build())
			.withOcspResponse("ocspResponse")
			.withOrderRef("orderRef")
			.withSignature("signature")
			.withSigned(NOW)
			.withStatus("status")
			.withStepUp(StepUpBuilder.create()
				.withMrtd(true)
				.build())
			.withUser(UserBuilder.create()
				.withGivenName("givenName")
				.withName("name")
				.withPersonalIdentityNumber("ersonalIdentityNumber")
				.withSurname("surname")
				.build())
			.build();
	}

	public static SupportInformation createSupportInformationEmbeddable() {
		return SupportInformation.create()
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

	public static OrganizationEntity createOrganizationEntity(LetterEntity letterEntity) {
		return OrganizationEntity.create()
			.withId("uuid")
			.withLetters(List.of(letterEntity))
			.withName("organization name")
			.withNumber(234L);
	}

	public static Organization createOrganization() {
		return OrganizationBuilder.create()
			.withName("organization-name")
			.withNumber(234L)
			.build();
	}
}
