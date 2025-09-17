package se.sundsvall.digitalregisteredletter.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;
import se.sundsvall.digitalregisteredletter.api.model.AttachmentBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterBuilder;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.model.LettersBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Organization;
import se.sundsvall.digitalregisteredletter.api.model.SupportInfoBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.OrganizationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SupportInfo;
import se.sundsvall.digitalregisteredletter.integration.db.model.UserEntity;

public final class LetterMapper {

	private LetterMapper() {}

	/*
	 * Methods for mapping API objects to their entity counterparts
	 */

	public static LetterEntity toLetterEntity(final LetterRequest nullableLetterRequest) {
		return ofNullable(nullableLetterRequest)
			.map(letterRequest -> LetterEntity.create()
				.withBody(letterRequest.body())
				.withContentType(letterRequest.contentType())
				.withPartyId(letterRequest.partyId())
				.withSubject(letterRequest.subject())
				.withSupportInfo(toSupportInfo(letterRequest.supportInfo())))
			.orElse(null);
	}

	public static OrganizationEntity toOrganizationEntity(Organization nullableOrganization, LetterEntity nullableLetterEntity) {
		return ofNullable(nullableOrganization)
			.map(organization -> OrganizationEntity.create()
				.withLetters(ofNullable(nullableLetterEntity).map(List::of).map(ArrayList::new).orElse(new ArrayList<>()))
				.withName(organization.name())
				.withNumber(organization.number()))
			.orElse(null);
	}

	public static UserEntity toUserEntity(String nullableUsername, LetterEntity nullableLetterEntity) {
		return ofNullable(nullableUsername)
			.map(username -> UserEntity.create()
				.withLetters(ofNullable(nullableLetterEntity).map(List::of).map(ArrayList::new).orElse(new ArrayList<>()))
				.withUsername(username))
			.orElse(null);
	}

	public static OrganizationEntity addLetter(OrganizationEntity organizationEntity, LetterEntity nullableLetterEntity) {
		if (isNull(organizationEntity.getLetters())) {
			organizationEntity.setLetters(new ArrayList<>());
		}

		ofNullable(nullableLetterEntity).ifPresent(letterEntity -> organizationEntity.getLetters().add(letterEntity));
		return organizationEntity;
	}

	public static UserEntity addLetter(UserEntity userEntity, LetterEntity nullableLetterEntity) {
		if (isNull(userEntity.getLetters())) {
			userEntity.setLetters(new ArrayList<>());
		}

		ofNullable(nullableLetterEntity).ifPresent(letterEntity -> userEntity.getLetters().add(letterEntity));
		return userEntity;
	}

	public static SupportInfo toSupportInfo(final se.sundsvall.digitalregisteredletter.api.model.SupportInfo nullableSupportInfo) {
		return ofNullable(nullableSupportInfo)
			.map(supportInfo -> SupportInfo.create()
				.withSupportText(supportInfo.supportText())
				.withContactInformationUrl(supportInfo.contactInformationUrl())
				.withContactInformationEmail(supportInfo.contactInformationEmail())
				.withContactInformationPhoneNumber(supportInfo.contactInformationPhoneNumber()))
			.orElse(null);
	}

	/*
	 * Methods for mapping database entities to their API counterparts
	 */

	public static Letters toLetters(final Page<LetterEntity> nullablePage) {
		return ofNullable(nullablePage)
			.map(page -> LettersBuilder.create()
				.withMetaData(PagingAndSortingMetaData.create().withPageData(page))
				.withLetters(toLetterList(page.getContent()))
				.build())
			.orElse(null);
	}

	private static List<Letter> toLetterList(final List<LetterEntity> nullableLetterEntities) {
		return ofNullable(nullableLetterEntities).orElse(emptyList()).stream()
			.map(LetterMapper::toLetter)
			.filter(Objects::nonNull)
			.toList();
	}

	public static Letter toLetter(final LetterEntity nullableLetterEntity) {
		return ofNullable(nullableLetterEntity)
			.map(etterEntity -> LetterBuilder.create()
				.withId(etterEntity.getId())
				.withMunicipalityId(etterEntity.getMunicipalityId())
				.withBody(etterEntity.getBody())
				.withContentType(etterEntity.getContentType())
				.withStatus(etterEntity.getStatus())
				.withAttachments(toLetterAttachments(etterEntity.getAttachments()))
				.withSupportInfo(toSupportInfo(etterEntity.getSupportInfo()))
				.withCreated(etterEntity.getCreated())
				.withUpdated(etterEntity.getUpdated())
				.build())
			.orElse(null);
	}

	public static se.sundsvall.digitalregisteredletter.api.model.SupportInfo toSupportInfo(final SupportInfo nullableSupportInfo) {
		return ofNullable(nullableSupportInfo)
			.map(supportInfo -> SupportInfoBuilder.create()
				.withContactInformationUrl(supportInfo.getContactInformationUrl())
				.withContactInformationEmail(supportInfo.getContactInformationEmail())
				.withContactInformationPhoneNumber(supportInfo.getContactInformationPhoneNumber())
				.withSupportText(supportInfo.getSupportText())
				.build())
			.orElse(null);
	}

	public static List<Letter.Attachment> toLetterAttachments(final List<AttachmentEntity> nullableAttachmentEntities) {
		return ofNullable(nullableAttachmentEntities).orElse(emptyList()).stream()
			.map(LetterMapper::toLetterAttachment)
			.filter(Objects::nonNull)
			.toList();
	}

	public static Letter.Attachment toLetterAttachment(final AttachmentEntity nullableAttachmentEntity) {
		return ofNullable(nullableAttachmentEntity)
			.map(attachmentEntity -> AttachmentBuilder.create()
				.withId(attachmentEntity.getId())
				.withFileName(attachmentEntity.getFileName())
				.withContentType(attachmentEntity.getContentType())
				.build())
			.orElse(null);
	}
}
