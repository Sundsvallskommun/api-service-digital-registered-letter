package se.sundsvall.digitalregisteredletter.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static se.sundsvall.TestDataFactory.createAttachmentEntity;
import static se.sundsvall.TestDataFactory.createLetterEntity;
import static se.sundsvall.TestDataFactory.createLetterRequest;
import static se.sundsvall.TestDataFactory.createSupportInformationEmbeddable;
import static se.sundsvall.digitalregisteredletter.Constants.STATUS_NOT_FOUND;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import se.sundsvall.digitalregisteredletter.api.model.Letter.Attachment;
import se.sundsvall.digitalregisteredletter.api.model.OrganizationBuilder;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfo;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfoBuilder;
import se.sundsvall.digitalregisteredletter.api.model.SupportInfoBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.OrganizationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SigningInformationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SupportInformation;
import se.sundsvall.digitalregisteredletter.integration.db.model.UserEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.BankIdOrderBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.CompletionDataBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.DeviceBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponse;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponseBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.SenderReferenceBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.StepUpBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.UserBuilder;

class LetterMapperTest {

	private final LetterMapper letterMapper = new LetterMapper();

	@Test
	void toLetterEntity() {
		final var letterRequest = createLetterRequest();

		final var result = letterMapper.toLetterEntity(letterRequest);

		assertThat(result.getBody()).isEqualTo(letterRequest.body());
		assertThat(result.getContentType()).isEqualTo(letterRequest.contentType());
		assertThat(result.getSupportInformation()).satisfies(assertedSupportInfo -> {
			assertThat(assertedSupportInfo.getSupportText()).isEqualTo(letterRequest.supportInfo().supportText());
			assertThat(assertedSupportInfo.getContactInformationEmail()).isEqualTo(letterRequest.supportInfo().contactInformationEmail());
			assertThat(assertedSupportInfo.getContactInformationUrl()).isEqualTo(letterRequest.supportInfo().contactInformationUrl());
			assertThat(assertedSupportInfo.getContactInformationPhoneNumber()).isEqualTo(letterRequest.supportInfo().contactInformationPhoneNumber());
		});

	}

	@Test
	void toLetterEntityFromNull() {
		assertThat(letterMapper.toLetterEntity(null)).isNull();
	}

	@Test
	void toSupportInformationEmbeddable() {
		final var supportInfo = SupportInfoBuilder.create()
			.withSupportText("supportText")
			.withContactInformationEmail("supportEmail")
			.withContactInformationUrl("supportUrl")
			.withContactInformationPhoneNumber("supportPhone")
			.build();

		final var result = letterMapper.toSupportInformation(supportInfo);

		assertThat(result.getSupportText()).isEqualTo(supportInfo.supportText());
		assertThat(result.getContactInformationEmail()).isEqualTo(supportInfo.contactInformationEmail());
		assertThat(result.getContactInformationUrl()).isEqualTo(supportInfo.contactInformationUrl());
		assertThat(result.getContactInformationPhoneNumber()).isEqualTo(supportInfo.contactInformationPhoneNumber());
	}

	@Test
	void toSupportInformationEmbeddableFromNull() {
		assertThat(letterMapper.toSupportInformation((se.sundsvall.digitalregisteredletter.api.model.SupportInfo) null)).isNull();
	}

	@Test
	void toOrganizationEntity() {
		final var name = "name";
		final var number = 911;
		final var letterEntity = LetterEntity.create();
		final var organization = OrganizationBuilder.create()
			.withName(name)
			.withNumber(number)
			.build();

		final var bean = letterMapper.toOrganizationEntity(organization, letterEntity);

		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("id");
		assertThat(bean.getLetters()).containsExactly(letterEntity);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getNumber()).isEqualTo(number);
	}

	@Test
	void toOrganizationEntityWithNullLetter() {
		final var name = "name";
		final var number = 911;
		final var organization = OrganizationBuilder.create()
			.withName(name)
			.withNumber(number)
			.build();

		final var bean = letterMapper.toOrganizationEntity(organization, null);

		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("id");
		assertThat(bean.getLetters()).isEmpty();
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getNumber()).isEqualTo(number);
	}

	@Test
	void toOrganizationEntityFromNull() {
		assertThat(letterMapper.toOrganizationEntity(null, null)).isNull();
		assertThat(letterMapper.toOrganizationEntity(null, LetterEntity.create())).isNull();
	}

	@Test
	void toUserEntity() {
		final var letterEntity = LetterEntity.create();
		final var username = "username";

		final var bean = letterMapper.toUserEntity(username, letterEntity);

		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("id");
		assertThat(bean.getUsername()).isEqualTo(username);
		assertThat(bean.getLetters()).containsExactly(letterEntity);
	}

	@Test
	void toUserEntityWithNullLetterList() {
		final var username = "username";

		final var bean = letterMapper.toUserEntity(username, null);

		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("id");
		assertThat(bean.getUsername()).isEqualTo(username);
		assertThat(bean.getLetters()).isEmpty();
	}

	@Test
	void toUserEntityFromNull() {
		assertThat(letterMapper.toUserEntity(null, null)).isNull();
		assertThat(letterMapper.toUserEntity(null, LetterEntity.create())).isNull();
	}

	@Test
	void addLetterToOrganizationEntity() {
		final var oldEntity = LetterEntity.create();
		final var newEntity = LetterEntity.create();
		final var organizationEntity = OrganizationEntity.create()
			.withLetters(new ArrayList<>(List.of(oldEntity)));

		final var bean = letterMapper.addLetter(organizationEntity, newEntity);

		assertThat(bean).isSameAs(organizationEntity);
		assertThat(bean.getLetters()).containsExactlyInAnyOrder(oldEntity, newEntity);
	}

	@Test
	void addNullToOrganizationEntityLetterList() {
		final var oldEntity = LetterEntity.create();
		final var organizationEntity = OrganizationEntity.create()
			.withLetters(new ArrayList<>(List.of(oldEntity)));

		final var bean = letterMapper.addLetter(organizationEntity, null);

		assertThat(bean).isSameAs(organizationEntity);
		assertThat(bean.getLetters()).containsExactlyInAnyOrder(oldEntity);
	}

	@Test
	void addLetterToOrganizationEntityWithNullList() {
		final var newEntity = LetterEntity.create();
		final var organizationEntity = OrganizationEntity.create()
			.withLetters(null);

		final var bean = letterMapper.addLetter(organizationEntity, newEntity);

		assertThat(bean).isSameAs(organizationEntity);
		assertThat(bean.getLetters()).containsExactlyInAnyOrder(newEntity);
	}

	@Test
	void addLetterToUserEntity() {
		final var oldEntity = LetterEntity.create();
		final var newEntity = LetterEntity.create();
		final var userEntity = UserEntity.create()
			.withLetters(new ArrayList<>(List.of(oldEntity)));

		final var bean = letterMapper.addLetter(userEntity, newEntity);

		assertThat(bean).isSameAs(userEntity);
		assertThat(bean.getLetters()).containsExactlyInAnyOrder(oldEntity, newEntity);
	}

	@Test
	void addNullToUserEntityLetterList() {
		final var oldEntity = LetterEntity.create();
		final var userEntity = UserEntity.create()
			.withLetters(new ArrayList<>(List.of(oldEntity)));

		final var bean = letterMapper.addLetter(userEntity, null);

		assertThat(bean).isSameAs(userEntity);
		assertThat(bean.getLetters()).containsExactlyInAnyOrder(oldEntity);
	}

	@Test
	void addLetterToUserEntityWithNullList() {
		final var newEntity = LetterEntity.create();
		final var userEntity = UserEntity.create()
			.withLetters(null);

		final var bean = letterMapper.addLetter(userEntity, newEntity);

		assertThat(bean).isSameAs(userEntity);
		assertThat(bean.getLetters()).containsExactlyInAnyOrder(newEntity);
	}

	@Test
	void toLetter() {
		final var entity = createLetterEntity();

		final var bean = letterMapper.toLetter(entity);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.subject()).isEqualTo("This is the subject of the letter");
		assertThat(bean.body()).isEqualTo("This is the body of the letter");
		assertThat(bean.contentType()).isEqualTo("text/plain");
		assertThat(bean.created()).isEqualTo(entity.getCreated());
		assertThat(bean.id()).isEqualTo("letter-id");
		assertThat(bean.municipalityId()).isEqualTo("municipality-id");
		assertThat(bean.status()).isEqualTo("NEW");
		assertThat(bean.updated()).isEqualTo(entity.getUpdated());
		assertThat(bean.supportInfo()).isNotNull()
			.extracting(
				se.sundsvall.digitalregisteredletter.api.model.SupportInfo::contactInformationEmail,
				se.sundsvall.digitalregisteredletter.api.model.SupportInfo::contactInformationPhoneNumber,
				se.sundsvall.digitalregisteredletter.api.model.SupportInfo::contactInformationUrl)
			.containsExactly(
				"support@email.com",
				"+46123456789",
				"https://example.com/support");
		assertThat(bean.attachments()).hasSize(1)
			.extracting(
				Attachment::contentType,
				Attachment::fileName,
				Attachment::id)
			.containsExactly(tuple(
				"text/plain",
				"file.txt",
				"attachment-id"));
	}

	@Test
	void toLetterFromNull() {
		assertThat(letterMapper.toLetter(null)).isNull();
	}

	@Test
	void toLetters() {
		final var entity = createLetterEntity();
		final var letter = letterMapper.toLetter(entity);

		final var pagedResponse = letterMapper.toLetters(new PageImpl<>(List.of(entity)));

		assertThat(pagedResponse).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(pagedResponse.letters()).hasSize(1).satisfiesExactly(assertedLetter -> {
			assertThat(assertedLetter).usingRecursiveAssertion().isEqualTo(letter);
		});
		assertThat(pagedResponse.metaData().getPage()).isEqualTo(1);
		assertThat(pagedResponse.metaData().getSortDirection()).isNull();
	}

	@Test
	void toLettersWithNullEntry() {
		final var entity = createLetterEntity();
		final var listWithNull = new ArrayList<>(List.of(entity));
		listWithNull.addFirst(null);

		final var pagedResponse = letterMapper.toLetters(new PageImpl<>(listWithNull));

		assertThat(pagedResponse).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(pagedResponse.letters()).hasSize(1);
	}

	@Test
	void toLettersFromNull() {
		assertThat(letterMapper.toLetters(null)).isNull();
	}

	@Test
	void toLetterStatus() {
		final var signingInformation = SigningInformationEntity.create()
			.withStatus("COMPLETED");
		final var letter = LetterEntity.create()
			.withId("letterId")
			.withStatus("SENT")
			.withSigningInformation(signingInformation);

		final var result = letterMapper.toLetterStatus(letter);

		assertThat(result.letterId()).isEqualTo(letter.getId());
		assertThat(result.status()).isEqualTo(letter.getStatus());
		assertThat(result.signingInformation()).isEqualTo(letter.getSigningInformation().getStatus());
	}

	@Test
	void toLetterStatus_withoutLetterStatus() {
		final var letter = LetterEntity.create()
			.withId("letterId")
			.withSigningInformation(SigningInformationEntity.create()
				.withStatus("COMPLETED"));

		final var result = letterMapper.toLetterStatus(letter);

		assertThat(result.letterId()).isEqualTo(letter.getId());
		assertThat(result.status()).isEqualTo(STATUS_NOT_FOUND);
		assertThat(result.signingInformation()).isEqualTo(letter.getSigningInformation().getStatus());
	}

	@Test
	void toLetterStatus_withoutSigningInformation() {
		final var letter = LetterEntity.create()
			.withId("letterId")
			.withStatus("SENT");

		final var result = letterMapper.toLetterStatus(letter);

		assertThat(result.letterId()).isEqualTo(letter.getId());
		assertThat(result.status()).isEqualTo(letter.getStatus());
		assertThat(result.signingInformation()).isEqualTo(STATUS_NOT_FOUND);
	}

	@Test
	void toLetterStatus_withNulls() {
		final var result = letterMapper.toLetterStatus("letterId", null, null);

		assertThat(result.letterId()).isEqualTo("letterId");
		assertThat(result.status()).isEqualTo(STATUS_NOT_FOUND);
		assertThat(result.signingInformation()).isEqualTo(STATUS_NOT_FOUND);
	}

	@Test
	void toSupportInfo() {
		final var embeddable = createSupportInformationEmbeddable();

		final var bean = letterMapper.toSupportInfo(embeddable);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.contactInformationEmail()).isEqualTo("support@email.com");
		assertThat(bean.contactInformationPhoneNumber()).isEqualTo("+46123456789");
		assertThat(bean.contactInformationUrl()).isEqualTo("https://example.com/support");
	}

	@Test
	void toSupportInfoFromNull() {
		assertThat(letterMapper.toSupportInfo((SupportInformation) null)).isNull();
	}

	@Test
	void toLetterAttachments() {
		final var entity = createAttachmentEntity();
		final var attachment = letterMapper.toLetterAttachment(entity);

		final var list = letterMapper.toLetterAttachments(List.of(entity));

		assertThat(list).hasSize(1).satisfiesExactly(assertedAttachment -> {
			assertThat(assertedAttachment).usingRecursiveAssertion().isEqualTo(attachment);
		});
	}

	@Test
	void toLetterAttachmentsWithNullEntry() {
		final var entity = createAttachmentEntity();
		final var listWithNull = new ArrayList<>(List.of(entity));
		listWithNull.addFirst(null);

		assertThat(letterMapper.toLetterAttachments(listWithNull)).hasSize(1);
	}

	@Test
	void toLetterAttachmentsFromNull() {
		assertThat(letterMapper.toLetterAttachments(null)).isEmpty();
	}

	@Test
	void toLetterAttachment() {
		final var entity = createAttachmentEntity();

		final var bean = letterMapper.toLetterAttachment(entity);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.contentType()).isEqualTo("text/plain");
		assertThat(bean.fileName()).isEqualTo("file.txt");
		assertThat(bean.id()).isEqualTo("attachment-id");
	}

	@Test
	void toLetterAttachmentFromNull() {
		assertThat(letterMapper.toLetterAttachment(null)).isNull();
	}

	@Test
	void updateSigningInformationWithNullValues() {
		final var responseMock = Mockito.mock(RegisteredLetterResponse.class);
		final var entityMock = Mockito.mock(SigningInformationEntity.class);

		assertDoesNotThrow(() -> letterMapper.updateSigningInformation(null, null));
		assertDoesNotThrow(() -> letterMapper.updateSigningInformation(null, responseMock));
		assertDoesNotThrow(() -> letterMapper.updateSigningInformation(entityMock, null));

		verifyNoInteractions(responseMock, entityMock);
	}

	/**
	 * Method for validating that mapper works as expected when mapping different combinations of RegisteredLetterResponse
	 * data to SigningInformationEntity data
	 *
	 * @param response       object to use in test
	 * @param expectedResult object to verify mapping result against
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("updateSigningInformationParameterProvider")
	void updateSigningInformation(final String testName, final RegisteredLetterResponse response, final SigningInformationEntity expectedResult) {
		final var entity = SigningInformationEntity.create();
		letterMapper.updateSigningInformation(entity, response);

		assertThat(entity).usingRecursiveAssertion().isEqualTo(expectedResult);
	}

	private static Stream<Arguments> updateSigningInformationParameterProvider() {
		final var contentKey = "contentKey";
		final var signedAt = OffsetDateTime.now();
		final var status = "status";
		final var internalId = "internalId";
		final var ocspResponse = "ocspResponse";
		final var orderRef = "orderRef";
		final var signature = "signature";
		final var mrtd = true;
		final var ipAddress = "ipAddress";
		final var givenName = "givenName";
		final var name = "name";
		final var personalNumber = "personalNumber";
		final var surname = "surname";

		return Stream.of(
			Arguments.of(
				"Empty response",
				RegisteredLetterResponseBuilder.create().build(),
				SigningInformationEntity.create()),

			Arguments.of(
				"Response with values on top level",
				RegisteredLetterResponseBuilder.create()
					.withContentKey(contentKey)
					.withSignedAt(signedAt)
					.withStatus(status) // This should not be mapped into the signing information entity, thus it is not added to the expected result
					.build(),
				SigningInformationEntity.create()
					.withContentKey(contentKey)
					.withSigned(signedAt)),

			Arguments.of(
				"Response with empty sender reference",
				RegisteredLetterResponseBuilder.create()
					.withSenderReference(SenderReferenceBuilder.create()
						.build())
					.build(),
				SigningInformationEntity.create()),

			Arguments.of(
				"Response with values for sender reference values",
				RegisteredLetterResponseBuilder.create()
					.withSenderReference(SenderReferenceBuilder.create()
						.withInternalId(internalId)
						.build())
					.build(),
				SigningInformationEntity.create()
					.withInternalId(internalId)),

			Arguments.of(
				"Response with empty bank id order",
				RegisteredLetterResponseBuilder.create()
					.withBankIdOrder(BankIdOrderBuilder.create()
						.build())
					.build(),
				SigningInformationEntity.create()),

			Arguments.of(
				"Response with values for bank id order",
				RegisteredLetterResponseBuilder.create()
					.withBankIdOrder(BankIdOrderBuilder.create()
						.withOrderRef(orderRef)
						.withStatus(status)
						.build())
					.build(),
				SigningInformationEntity.create()
					.withOrderRef(orderRef)
					.withStatus(status.toUpperCase())),

			Arguments.of(
				"Response with empty step up",
				RegisteredLetterResponseBuilder.create()
					.withBankIdOrder(BankIdOrderBuilder.create()
						.withCompletionData(CompletionDataBuilder.create()
							.withStepUp(StepUpBuilder.create()
								.build())
							.build())
						.build())
					.build(),
				SigningInformationEntity.create()),

			Arguments.of(
				"Response with values for step up",
				RegisteredLetterResponseBuilder.create()
					.withBankIdOrder(BankIdOrderBuilder.create()
						.withCompletionData(CompletionDataBuilder.create()
							.withStepUp(StepUpBuilder.create()
								.withMrtd(mrtd)
								.build())
							.build())
						.build())
					.build(),
				SigningInformationEntity.create()
					.withMrtd(mrtd)),

			Arguments.of(
				"Response with empty completion data",
				RegisteredLetterResponseBuilder.create()
					.withBankIdOrder(BankIdOrderBuilder.create()
						.withCompletionData(CompletionDataBuilder.create().build())
						.build())
					.build(),
				SigningInformationEntity.create()),

			Arguments.of(
				"Response with empty device and user in completion data object",
				RegisteredLetterResponseBuilder.create()
					.withBankIdOrder(BankIdOrderBuilder.create()
						.withCompletionData(CompletionDataBuilder.create()
							.withDevice(DeviceBuilder.create().build())
							.withUser(UserBuilder.create().build())
							.build())
						.build())
					.build(),
				SigningInformationEntity.create()),

			Arguments.of(
				"Response with values for signature and ocsp response in completion data object",
				RegisteredLetterResponseBuilder.create()
					.withBankIdOrder(BankIdOrderBuilder.create()
						.withCompletionData(CompletionDataBuilder.create()
							.withSignature(signature)
							.withOcspResponse(ocspResponse)
							.build())
						.build())
					.build(),
				SigningInformationEntity.create()
					.withSignature(signature)
					.withOcspResponse(ocspResponse)),

			Arguments.of(
				"Response with values for StepUp in completion data object",
				RegisteredLetterResponseBuilder.create()
					.withBankIdOrder(BankIdOrderBuilder.create()
						.withCompletionData(CompletionDataBuilder.create()
							.withStepUp(StepUpBuilder.create()
								.withMrtd(mrtd)
								.build())
							.build())
						.build())
					.build(),
				SigningInformationEntity.create()
					.withMrtd(mrtd)),

			Arguments.of(
				"Response with values for device in completion data object",
				RegisteredLetterResponseBuilder.create()
					.withBankIdOrder(BankIdOrderBuilder.create()
						.withCompletionData(CompletionDataBuilder.create()
							.withDevice(DeviceBuilder.create()
								.withIpAddress(ipAddress)
								.build())
							.build())
						.build())
					.build(),
				SigningInformationEntity.create()
					.withIpAddress(ipAddress)),

			Arguments.of(
				"Response with values for user in completion data object",
				RegisteredLetterResponseBuilder.create()
					.withBankIdOrder(BankIdOrderBuilder.create()
						.withCompletionData(CompletionDataBuilder.create()
							.withUser(UserBuilder.create()
								.withGivenName(givenName)
								.withName(name)
								.withPersonalNumber(personalNumber)
								.withSurname(surname)
								.build())
							.build())
						.build())
					.build(),
				SigningInformationEntity.create()
					.withGivenName(givenName)
					.withName(name)
					.withPersonalNumber(personalNumber)
					.withSurname(surname)));
	}

	/**
	 * Method for validating that mapper works as expected when mapping different combinations of SigningInformationEntity
	 * data to SigningInfo data
	 *
	 * @param entity         object to use in test
	 * @param expectedResult object to verify map result against
	 */
	@ParameterizedTest
	@MethodSource("toSigningInfoParameterProvider")
	void toSigningInfo(final SigningInformationEntity entity, final SigningInfo expectedResult) {
		assertThat(letterMapper.toSigningInfo(entity)).usingRecursiveAssertion().isEqualTo(expectedResult);
	}

	private static Stream<Arguments> toSigningInfoParameterProvider() {
		final var contentKey = "contentKey";
		final var signed = OffsetDateTime.now();
		final var status = "status";
		final var ocspResponse = "ocspResponse";
		final var orderRef = "orderRef";
		final var signature = "signature";
		final var mrtd = true;
		final var ipAddress = "ipAddress";
		final var givenName = "givenName";
		final var name = "name";
		final var personalNumber = "personalNumber";
		final var surname = "surname";

		return Stream.of(
			// Null response
			Arguments.of(null, null),

			// Empty response
			Arguments.of(SigningInformationEntity.create(), SigningInfoBuilder.create().build()),

			// Response with values on top level
			Arguments.of(
				SigningInformationEntity.create()
					.withContentKey(contentKey)
					.withOcspResponse(ocspResponse)
					.withOrderRef(orderRef)
					.withSignature(signature)
					.withSigned(signed)
					.withStatus(status),
				SigningInfoBuilder.create()
					.withContentKey(contentKey)
					.withOcspResponse(ocspResponse)
					.withOrderRef(orderRef)
					.withSignature(signature)
					.withSigned(signed)
					.withStatus(status).build()),

			// Response with values for step up section
			Arguments.of(
				SigningInformationEntity.create()
					.withMrtd(mrtd),
				SigningInfoBuilder.create()
					.withStepUp(se.sundsvall.digitalregisteredletter.api.model.StepUpBuilder.create()
						.withMrtd(mrtd)
						.build())
					.build()),

			// Response with values for device section
			Arguments.of(
				SigningInformationEntity.create()
					.withIpAddress(ipAddress),
				SigningInfoBuilder.create()
					.withDevice(se.sundsvall.digitalregisteredletter.api.model.DeviceBuilder.create()
						.withIpAddress(ipAddress)
						.build())
					.build()),

			// Response with all values set for signatory user section
			Arguments.of(
				SigningInformationEntity.create()
					.withGivenName(givenName)
					.withName(name)
					.withPersonalNumber(personalNumber)
					.withSurname(surname),
				SigningInfoBuilder.create()
					.withUser(se.sundsvall.digitalregisteredletter.api.model.UserBuilder.create()
						.withGivenName(givenName)
						.withName(name)
						.withPersonalIdentityNumber(personalNumber)
						.withSurname(surname)
						.build())
					.build()),

			// Response with only value for given name set for signatory user section
			Arguments.of(
				SigningInformationEntity.create()
					.withGivenName(givenName),
				SigningInfoBuilder.create()
					.withUser(se.sundsvall.digitalregisteredletter.api.model.UserBuilder.create()
						.withGivenName(givenName)
						.build())
					.build()),

			// Response with only value for name set for signatory user section
			Arguments.of(
				SigningInformationEntity.create()
					.withName(name),
				SigningInfoBuilder.create()
					.withUser(se.sundsvall.digitalregisteredletter.api.model.UserBuilder.create()
						.withName(name)
						.build())
					.build()),

			// Response with only value for personal identity number set for signatory user section
			Arguments.of(
				SigningInformationEntity.create()
					.withPersonalNumber(personalNumber),
				SigningInfoBuilder.create()
					.withUser(se.sundsvall.digitalregisteredletter.api.model.UserBuilder.create()
						.withPersonalIdentityNumber(personalNumber)
						.build())
					.build()),

			// Response with only value for surname set for signatory user section
			Arguments.of(
				SigningInformationEntity.create()
					.withSurname(surname),
				SigningInfoBuilder.create()
					.withUser(se.sundsvall.digitalregisteredletter.api.model.UserBuilder.create()
						.withSurname(surname)
						.build())
					.build()));
	}
}
