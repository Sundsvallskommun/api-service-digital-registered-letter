package se.sundsvall.digitalregisteredletter.integration.kivra;

import static java.util.Collections.emptyList;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.sundsvall.digitalregisteredletter.integration.db.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserV2;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserV2Builder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.PartsResponsiveBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterHiddenBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.UserMatchV2SSN;
import se.sundsvall.digitalregisteredletter.service.util.BlobUtil;

@Component
public class KivraMapper {

	private final BlobUtil blobUtil;

	public KivraMapper(final BlobUtil blobUtil) {
		this.blobUtil = blobUtil;
	}

	UserMatchV2SSN toCheckEligibilityRequest(final List<String> legalIds) {
		return new UserMatchV2SSN(legalIds);
	}

	ContentUserV2 toSendContentRequest(final LetterEntity letterEntity, final String legalId) {
		return ContentUserV2Builder.create()
			.withLegalId(legalId)
			.withSubject(letterEntity.getSubject())
			.withType("registered.letter")
			.withRegistered(toRegisteredLetter(letterEntity.getId()))
			.withParts(toPartsResponsives(letterEntity.getAttachments()))
			.build();
	}

	/**
	 * Creates a RegisteredLetter object that configures the letter visibility in the Kivra inbox and sets the expiration
	 * date. A sender reference is also set to allow us to fetch the letter status separately.
	 *
	 * @return RegisteredLetter with visibility settings, expiration date and a sender reference.
	 */
	ContentUserV2.RegisteredLetter toRegisteredLetter(final String reference) {
		return RegisteredLetterBuilder.create()
			.withExpiresAt(OffsetDateTime.now().plusDays(30))
			.withSenderReference(new ContentUserV2.RegisteredLetter.SenderReference(reference))
			.withHidden(createRegisteredLetterHidden())
			.build();
	}

	/**
	 * RegisteredLetterHidden is used to configure if the subject and sender should be hidden in the Kivra inbox before the
	 * letter is opened.
	 *
	 * @return RegisteredLetterHidden object that configures the visibility of the subject and sender in the Kivra inbox.
	 */
	ContentUserV2.RegisteredLetter.RegisteredLetterHidden createRegisteredLetterHidden() {
		return RegisteredLetterHiddenBuilder.create()
			// Setting sender to true means that the sender name and icon will be hidden in the Kivra Inbox.
			.withSender(false)
			// Setting subject to true means that the subject will be hidden in the Kivra Inbox.
			.withSubject(false)
			.build();
	}

	List<ContentUserV2.PartsResponsive> toPartsResponsives(final List<AttachmentEntity> entities) {
		return Optional.ofNullable(entities).orElse(emptyList()).stream()
			.map(this::toPartsResponsive)
			.filter(Objects::nonNull)
			.toList();
	}

	/**
	 * Maps an AttachmentEntity to a PartsResponsive object that represents an attachment in Kivra.
	 *
	 * @param  attachmentEntity the AttachmentEntity to map
	 * @return                  a PartsResponsive object containing the attachment's name, content (as a Base64 string), and
	 *                          content type,
	 */
	ContentUserV2.PartsResponsive toPartsResponsive(final AttachmentEntity attachmentEntity) {
		return Optional.ofNullable(attachmentEntity).map(attachment -> PartsResponsiveBuilder.create()
			.withName(attachment.getFileName())
			.withData(blobUtil.convertBlobToBase64String(attachment.getContent()))
			.withContentType(attachment.getContentType())
			.build())
			.orElse(null);
	}
}
