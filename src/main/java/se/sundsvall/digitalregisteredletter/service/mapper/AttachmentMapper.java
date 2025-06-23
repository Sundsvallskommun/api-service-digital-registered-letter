package se.sundsvall.digitalregisteredletter.service.mapper;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.digitalregisteredletter.api.model.Attachments;
import se.sundsvall.digitalregisteredletter.integration.db.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.service.util.BlobUtil;

@Component
public final class AttachmentMapper {

	private final BlobUtil blobUtil;

	public AttachmentMapper(final BlobUtil blobUtil) {
		this.blobUtil = blobUtil;
	}

	public List<AttachmentEntity> toAttachmentEntities(final Attachments attachments) {
		return Optional.ofNullable(attachments).map(Attachments::files)
			.map(files -> files.stream()
				.map(this::toAttachmentEntity)
				.toList())
			.orElse(null);
	}

	public AttachmentEntity toAttachmentEntity(final MultipartFile multipartFile) {
		return Optional.ofNullable(multipartFile)
			.map(file -> new AttachmentEntity()
				.withFileName(file.getOriginalFilename())
				.withContentType(file.getContentType())
				.withContent(blobUtil.convertToBlob(file)))
			.orElse(null);
	}

}
