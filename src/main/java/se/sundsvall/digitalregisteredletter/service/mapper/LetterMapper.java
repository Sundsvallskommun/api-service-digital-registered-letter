package se.sundsvall.digitalregisteredletter.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.allNull;
import static org.apache.commons.lang3.ObjectUtils.anyNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;
import se.sundsvall.digitalregisteredletter.api.model.AttachmentBuilder;
import se.sundsvall.digitalregisteredletter.api.model.DeviceBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterBuilder;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.model.LettersBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Organization;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfo;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfo.Device;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfo.StepUp;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfo.User;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfoBuilder;
import se.sundsvall.digitalregisteredletter.api.model.StepUpBuilder;
import se.sundsvall.digitalregisteredletter.api.model.SupportInfoBuilder;
import se.sundsvall.digitalregisteredletter.api.model.UserBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.OrganizationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SigningInformationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SupportInformation;
import se.sundsvall.digitalregisteredletter.integration.db.model.UserEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponse;

@Component
public class LetterMapper {

	/*
	 * Methods for mapping API objects to their entity counterparts
	 */

	public LetterEntity toLetterEntity(final LetterRequest nullableLetterRequest) {
		return ofNullable(nullableLetterRequest)
			.map(letterRequest -> LetterEntity.create()
				.withBody(letterRequest.body())
				.withContentType(letterRequest.contentType())
				.withPartyId(letterRequest.partyId())
				.withSubject(letterRequest.subject())
				.withSupportInformation(toSupportInformation(letterRequest.supportInfo())))
			.orElse(null);
	}

	public OrganizationEntity toOrganizationEntity(Organization nullableOrganization, LetterEntity nullableLetterEntity) {
		return ofNullable(nullableOrganization)
			.map(organization -> OrganizationEntity.create()
				.withLetters(ofNullable(nullableLetterEntity).map(List::of).map(ArrayList::new).orElse(new ArrayList<>()))
				.withName(organization.name())
				.withNumber(organization.number()))
			.orElse(null);
	}

	public UserEntity toUserEntity(String nullableUsername, LetterEntity nullableLetterEntity) {
		return ofNullable(nullableUsername)
			.map(username -> UserEntity.create()
				.withLetters(ofNullable(nullableLetterEntity).map(List::of).map(ArrayList::new).orElse(new ArrayList<>()))
				.withUsername(username))
			.orElse(null);
	}

	public OrganizationEntity addLetter(OrganizationEntity organizationEntity, LetterEntity nullableLetterEntity) {
		if (isNull(organizationEntity.getLetters())) {
			organizationEntity.setLetters(new ArrayList<>());
		}

		ofNullable(nullableLetterEntity).ifPresent(letterEntity -> organizationEntity.getLetters().add(letterEntity));
		return organizationEntity;
	}

	public UserEntity addLetter(UserEntity userEntity, LetterEntity nullableLetterEntity) {
		if (isNull(userEntity.getLetters())) {
			userEntity.setLetters(new ArrayList<>());
		}

		ofNullable(nullableLetterEntity).ifPresent(letterEntity -> userEntity.getLetters().add(letterEntity));
		return userEntity;
	}

	public SupportInformation toSupportInformation(final se.sundsvall.digitalregisteredletter.api.model.SupportInfo nullableSupportInfo) {
		return ofNullable(nullableSupportInfo)
			.map(supportInfo -> SupportInformation.create()
				.withSupportText(supportInfo.supportText())
				.withContactInformationUrl(supportInfo.contactInformationUrl())
				.withContactInformationEmail(supportInfo.contactInformationEmail())
				.withContactInformationPhoneNumber(supportInfo.contactInformationPhoneNumber()))
			.orElse(null);
	}

	/*
	 * Methods for mapping database entities to their API counterparts
	 */

	public Letters toLetters(final Page<LetterEntity> nullablePage) {
		return ofNullable(nullablePage)
			.map(page -> LettersBuilder.create()
				.withMetaData(PagingAndSortingMetaData.create().withPageData(page))
				.withLetters(toLetterList(page.getContent()))
				.build())
			.orElse(null);
	}

	private List<Letter> toLetterList(final List<LetterEntity> nullableLetterEntities) {
		return ofNullable(nullableLetterEntities).orElse(emptyList()).stream()
			.map(this::toLetter)
			.filter(Objects::nonNull)
			.toList();
	}

	public Letter toLetter(final LetterEntity nullableLetterEntity) {
		return ofNullable(nullableLetterEntity)
			.map(etterEntity -> LetterBuilder.create()
				.withId(etterEntity.getId())
				.withMunicipalityId(etterEntity.getMunicipalityId())
				.withBody(etterEntity.getBody())
				.withContentType(etterEntity.getContentType())
				.withStatus(etterEntity.getStatus())
				.withAttachments(toLetterAttachments(etterEntity.getAttachments()))
				.withSupportInfo(toSupportInfo(etterEntity.getSupportInformation()))
				.withCreated(etterEntity.getCreated())
				.withUpdated(etterEntity.getUpdated())
				.build())
			.orElse(null);
	}

	public se.sundsvall.digitalregisteredletter.api.model.SupportInfo toSupportInfo(final SupportInformation nullableSupportInformation) {
		return ofNullable(nullableSupportInformation)
			.map(supportInformation -> SupportInfoBuilder.create()
				.withContactInformationUrl(supportInformation.getContactInformationUrl())
				.withContactInformationEmail(supportInformation.getContactInformationEmail())
				.withContactInformationPhoneNumber(supportInformation.getContactInformationPhoneNumber())
				.withSupportText(supportInformation.getSupportText())
				.build())
			.orElse(null);
	}

	public List<Letter.Attachment> toLetterAttachments(final List<AttachmentEntity> nullableAttachmentEntities) {
		return ofNullable(nullableAttachmentEntities).orElse(emptyList()).stream()
			.map(this::toLetterAttachment)
			.filter(Objects::nonNull)
			.toList();
	}

	public Letter.Attachment toLetterAttachment(final AttachmentEntity nullableAttachmentEntity) {
		return ofNullable(nullableAttachmentEntity)
			.map(attachmentEntity -> AttachmentBuilder.create()
				.withId(attachmentEntity.getId())
				.withFileName(attachmentEntity.getFileName())
				.withContentType(attachmentEntity.getContentType())
				.build())
			.orElse(null);
	}

	public void updateLetterStatus(final LetterEntity letter, String status) {
		if (isNull(letter)) {
			return;
		}

		ofNullable(status).ifPresent(value -> letter.setStatus(value.toUpperCase()));
	}

	public void updateSigningInformation(final SigningInformationEntity signingInformation, final RegisteredLetterResponse kivraResponse) {
		if (anyNull(signingInformation, kivraResponse)) {
			return;
		}

		ofNullable(kivraResponse.contentKey()).ifPresent(signingInformation::setContentKey);
		ofNullable(kivraResponse.signedAt()).ifPresent(signingInformation::setSigned);
		ofNullable(kivraResponse.senderReference()).ifPresent(value -> signingInformation.setInternalId(value.internalId()));
		ofNullable(kivraResponse.bankIdOrder()).ifPresent(value -> updateBankIdOrder(signingInformation, value));
	}

	private void updateBankIdOrder(final SigningInformationEntity signingInformation, final RegisteredLetterResponse.BankIdOrder bankIdOrder) {
		ofNullable(bankIdOrder.ocspResponse()).ifPresent(signingInformation::setOcspResponse);
		ofNullable(bankIdOrder.orderRef()).ifPresent(signingInformation::setOrderRef);
		ofNullable(bankIdOrder.signature()).ifPresent(signingInformation::setSignature);
		ofNullable(bankIdOrder.status()).ifPresent(value -> signingInformation.setStatus(value.toUpperCase()));
		ofNullable(bankIdOrder.completionData()).ifPresent(value -> updateCompletionData(signingInformation, value));
		ofNullable(bankIdOrder.stepUp()).ifPresent(value -> signingInformation.setMrtd(value.mrtd()));
	}

	private void updateCompletionData(final SigningInformationEntity signingInformation, final RegisteredLetterResponse.BankIdOrder.CompletionData completionData) {
		ofNullable(completionData.device()).ifPresent(value -> signingInformation.setIpAddress(value.ipAddress()));
		ofNullable(completionData.user()).ifPresent(value -> {
			signingInformation.setGivenName(value.givenName());
			signingInformation.setName(value.name());
			signingInformation.setPersonalNumber(value.personalNumber());
			signingInformation.setSurname(value.surname());
		});
	}

	public SigningInfo toSigningInfo(SigningInformationEntity nullableSigningInformation) {
		return ofNullable(nullableSigningInformation)
			.map(signingInformation -> SigningInfoBuilder.create()
				.withContentKey(signingInformation.getContentKey())
				.withDevice(toDevice(signingInformation.getIpAddress()))
				.withOcspResponse(signingInformation.getOcspResponse())
				.withOrderRef(signingInformation.getOrderRef())
				.withSignature(signingInformation.getSignature())
				.withSigned(signingInformation.getSigned())
				.withStatus(signingInformation.getStatus())
				.withStepUp(toStepUp(signingInformation.getMrtd()))
				.withUser(toUser(
					signingInformation.getGivenName(),
					signingInformation.getName(),
					signingInformation.getPersonalNumber(),
					signingInformation.getSurname()))
				.build())
			.orElse(null);
	}

	private Device toDevice(String nullableIpAddress) {
		return ofNullable(nullableIpAddress)
			.map(ipAddress -> DeviceBuilder.create()
				.withIpAddress(ipAddress)
				.build())
			.orElse(null);
	}

	private StepUp toStepUp(Boolean nullableMrtd) {
		return ofNullable(nullableMrtd)
			.map(mrtd -> StepUpBuilder.create()
				.withMrtd(mrtd)
				.build())
			.orElse(null);
	}

	private User toUser(String givenName, String name, String personalIdentityNumber, String surname) {
		if (allNull(givenName, name, personalIdentityNumber, surname)) {
			return null;
		}

		return UserBuilder.create()
			.withGivenName(givenName)
			.withName(name)
			.withPersonalIdentityNumber(personalIdentityNumber)
			.withSurname(surname)
			.build();
	}
}
