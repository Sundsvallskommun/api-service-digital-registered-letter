package se.sundsvall.digitalregisteredletter.integration.kivra;

import static java.util.Collections.emptyList;
import static se.sundsvall.digitalregisteredletter.service.util.BlobUtil.convertBlobToBase64String;

import generated.com.kivra.ContentUserV2;
import generated.com.kivra.PartsResponsive;
import generated.com.kivra.RegisteredLetter;
import generated.com.kivra.RegisteredLetterHidden;
import generated.com.kivra.UserMatchV2SSN;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.sundsvall.digitalregisteredletter.integration.db.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.LetterEntity;

public final class KivraMapper {

	private KivraMapper() {}

	public static UserMatchV2SSN toCheckEligibilityRequest(final List<String> legalIds) {
		var eligibilityRequest = new UserMatchV2SSN();
		eligibilityRequest.setList(legalIds);
		return eligibilityRequest;
	}

	public static ContentUserV2 toSendContentRequest(final LetterEntity letterEntity, final String legalId) {
		var contentRequest = new ContentUserV2();
		contentRequest.setSsn(legalId);
		contentRequest.setSubject(letterEntity.getSubject());
		contentRequest.setType("registered.letter");
		contentRequest.setParts(toPartsResponsives(letterEntity.getAttachments()));
		contentRequest.setRegistered(toRegisteredLetter(letterEntity.getId()));

		return contentRequest;
	}

	/**
	 * Creates a RegisteredLetter object that configures the letter visibility in the Kivra inbox and sets the expiration
	 * date. A sender reference is also set to allow us to fetch the letter status separately.
	 *
	 * @return RegisteredLetter with visibility settings, expiration date and a sender reference.
	 */
	public static RegisteredLetter toRegisteredLetter(final String reference) {
		var registeredLetter = new RegisteredLetter();
		registeredLetter.setExpiresAt(LocalDateTime.now().plusDays(30).format(DateTimeFormatter.ISO_DATE_TIME));
		registeredLetter.setHidden(createRegisteredLetterHidden());
		registeredLetter.setSenderReference(reference);
		return registeredLetter;
	}

	/**
	 * RegisteredLetterHidden is used to configure if the subject and sender should be hidden in the Kivra inbox before the
	 * letter is opened.
	 *
	 * @return RegisteredLetterHidden object that configures the visibility of the subject and sender in the Kivra inbox.
	 */
	public static RegisteredLetterHidden createRegisteredLetterHidden() {
		var hidden = new RegisteredLetterHidden();
		// Setting subject to true means that the subject will be hidden in the Kivra Inbox.
		hidden.setSubject(false);
		// Setting sender to true means that the sender name and icon will be hidden in the Kivra Inbox.
		hidden.setSender(false);
		return hidden;
	}

	public static List<PartsResponsive> toPartsResponsives(final List<AttachmentEntity> entities) {
		return Optional.ofNullable(entities).orElse(emptyList()).stream()
			.map(KivraMapper::toPartsResponsive)
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
	public static PartsResponsive toPartsResponsive(final AttachmentEntity attachmentEntity) {
		return Optional.ofNullable(attachmentEntity).map(attachment -> {
			var part = new PartsResponsive();
			part.setName(attachment.getFileName());
			part.setData(convertBlobToBase64String(attachment.getContent()));
			part.setContentType(attachment.getContentType());
			return part;
		})
			.orElse(null);
	}

}
