package se.sundsvall.digitalregisteredletter.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import se.sundsvall.digitalregisteredletter.api.model.AttachmentBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterBuilder;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
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

	public static LetterEntity toLetterEntity(final LetterRequest letterRequest) {
		return ofNullable(letterRequest)
			.map(lr -> LetterEntity.create()
				.withBody(lr.body())
				.withContentType(lr.contentType())
				.withPartyId(lr.partyId())
				.withSubject(lr.subject())
				.withSupportInfo(toSupportInfo(lr.supportInfo())))
			.orElse(null);
	}

	public static OrganizationEntity toOrganizationEntity(Organization organization, LetterEntity letterEntity) {
		return ofNullable(organization)
			.map(o -> OrganizationEntity.create()
				.withLetters(ofNullable(letterEntity).map(List::of).map(ArrayList::new).orElse(new ArrayList<>()))
				.withName(o.name())
				.withNumber(o.number()))
			.orElse(null);
	}

	public static UserEntity toUserEntity(String username, LetterEntity letterEntity) {
		return ofNullable(username)
			.map(u -> UserEntity.create()
				.withLetters(ofNullable(letterEntity).map(List::of).map(ArrayList::new).orElse(new ArrayList<>()))
				.withUsername(u))
			.orElse(null);
	}

	public static OrganizationEntity addLetter(OrganizationEntity organizationEntity, LetterEntity letterEntity) {
		if (isNull(organizationEntity.getLetters())) {
			organizationEntity.setLetters(new ArrayList<>());
		}

		ofNullable(letterEntity).ifPresent(le -> organizationEntity.getLetters().add(le));
		return organizationEntity;
	}

	public static UserEntity addLetter(UserEntity userEntity, LetterEntity letterEntity) {
		if (isNull(userEntity.getLetters())) {
			userEntity.setLetters(new ArrayList<>());
		}

		ofNullable(letterEntity).ifPresent(le -> userEntity.getLetters().add(le));
		return userEntity;
	}

	public static SupportInfo toSupportInfo(final se.sundsvall.digitalregisteredletter.api.model.SupportInfo supportInfo) {
		return ofNullable(supportInfo)
			.map(si -> SupportInfo.create()
				.withSupportText(si.supportText())
				.withContactInformationUrl(si.contactInformationUrl())
				.withContactInformationEmail(si.contactInformationEmail())
				.withContactInformationPhoneNumber(si.contactInformationPhoneNumber()))
			.orElse(null);
	}

	/*
	 * Methods for mapping database entities to their API counterparts
	 */

	public static List<Letter> toLetters(final List<LetterEntity> letterEntities) {
		return ofNullable(letterEntities).orElse(emptyList()).stream()
			.map(LetterMapper::toLetter)
			.filter(Objects::nonNull)
			.toList();
	}

	public static Letter toLetter(final LetterEntity letterEntity) {
		return ofNullable(letterEntity)
			.map(le -> LetterBuilder.create()
				.withId(le.getId())
				.withMunicipalityId(le.getMunicipalityId())
				.withBody(le.getBody())
				.withContentType(le.getContentType())
				.withStatus(le.getStatus())
				.withAttachments(toLetterAttachments(le.getAttachments()))
				.withSupportInfo(toSupportInfo(le.getSupportInfo()))
				.withCreated(le.getCreated())
				.withUpdated(le.getUpdated())
				.build())
			.orElse(null);
	}

	public static se.sundsvall.digitalregisteredletter.api.model.SupportInfo toSupportInfo(final SupportInfo supportInfo) {
		return ofNullable(supportInfo)
			.map(si -> SupportInfoBuilder.create()
				.withContactInformationUrl(si.getContactInformationUrl())
				.withContactInformationEmail(si.getContactInformationEmail())
				.withContactInformationPhoneNumber(si.getContactInformationPhoneNumber())
				.withSupportText(si.getSupportText())
				.build())
			.orElse(null);
	}

	public static List<Letter.Attachment> toLetterAttachments(final List<AttachmentEntity> attachmentEntities) {
		return ofNullable(attachmentEntities).orElse(emptyList()).stream()
			.map(LetterMapper::toLetterAttachment)
			.filter(Objects::nonNull)
			.toList();
	}

	public static Letter.Attachment toLetterAttachment(final AttachmentEntity attachmentEntity) {
		return ofNullable(attachmentEntity)
			.map(ae -> AttachmentBuilder.create()
				.withId(ae.getId())
				.withFileName(ae.getFileName())
				.withContentType(ae.getContentType())
				.build())
			.orElse(null);
	}
}
