package se.sundsvall.digitalregisteredletter.service.mapper;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;
import se.sundsvall.digitalregisteredletter.api.model.AttachmentBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterBuilder;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.SupportInfoBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SupportInfo;

public final class LetterMapper {

	private LetterMapper() {}

	public static LetterEntity toLetterEntity(final LetterRequest letterRequest) {
		return Optional.ofNullable(letterRequest).map(letter -> LetterEntity.create()
			.withBody(letter.body())
			.withContentType(letter.contentType())
			.withPartyId(letter.partyId())
			.withSubject(letter.subject())
			.withSupportInfo(toSupportInfo(letter.supportInfo())))
			.orElse(null);
	}

	public static SupportInfo toSupportInfo(final se.sundsvall.digitalregisteredletter.api.model.SupportInfo supportInfo) {
		return Optional.ofNullable(supportInfo).map(info -> SupportInfo.create()
			.withSupportText(info.supportText())
			.withContactInformationUrl(info.contactInformationUrl())
			.withContactInformationEmail(info.contactInformationEmail())
			.withContactInformationPhoneNumber(info.contactInformationPhoneNumber()))
			.orElse(null);
	}

	public static List<Letter> toLetters(final List<LetterEntity> letterEntities) {
		return Optional.ofNullable(letterEntities).orElse(emptyList()).stream()
			.map(LetterMapper::toLetter)
			.toList();
	}

	public static Letter toLetter(final LetterEntity letterEntity) {
		return Optional.ofNullable(letterEntity).map(letter -> LetterBuilder.create()
			.withId(letterEntity.getId())
			.withMunicipalityId(letterEntity.getMunicipalityId())
			.withBody(letterEntity.getBody())
			.withContentType(letterEntity.getContentType())
			.withStatus(letterEntity.getStatus())
			.withAttachments(toLetterAttachments(letterEntity.getAttachments()))
			.withSupportInfo(toSupportInfo(letterEntity.getSupportInfo()))
			.withCreated(letterEntity.getCreated())
			.withUpdated(letterEntity.getUpdated())
			.build())
			.orElse(null);
	}

	public static se.sundsvall.digitalregisteredletter.api.model.SupportInfo toSupportInfo(final SupportInfo supportInfo) {
		return Optional.ofNullable(supportInfo).map(info -> SupportInfoBuilder.create()
			.withContactInformationUrl(supportInfo.getContactInformationUrl())
			.withContactInformationEmail(supportInfo.getContactInformationEmail())
			.withContactInformationPhoneNumber(supportInfo.getContactInformationPhoneNumber())
			.withSupportText(supportInfo.getSupportText())
			.build())
			.orElse(null);
	}

	public static List<Letter.Attachment> toLetterAttachments(final List<AttachmentEntity> attachmentEntities) {
		return Optional.ofNullable(attachmentEntities).orElse(emptyList()).stream()
			.map(LetterMapper::toLetterAttachment)
			.toList();
	}

	public static Letter.Attachment toLetterAttachment(final AttachmentEntity attachmentEntity) {
		return Optional.ofNullable(attachmentEntity).map(attachment -> AttachmentBuilder.create()
			.withId(attachment.getId())
			.withFileName(attachment.getFileName())
			.withContentType(attachment.getContentType())
			.build())
			.orElse(null);
	}

}
