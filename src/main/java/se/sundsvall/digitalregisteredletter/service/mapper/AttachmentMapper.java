package se.sundsvall.digitalregisteredletter.service.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.service.model.AttachmentData;
import se.sundsvall.digitalregisteredletter.service.util.BlobUtil;

import static java.util.Collections.emptyList;

@Component
public final class AttachmentMapper {

	private final BlobUtil blobUtil;

	public AttachmentMapper(final BlobUtil blobUtil) {
		this.blobUtil = blobUtil;
	}

	public List<AttachmentEntity> toAttachmentEntities(final List<AttachmentData> attachments) {
		return Optional.ofNullable(attachments).orElse(emptyList()).stream()
			.map(this::toAttachmentEntity)
			.collect(Collectors.toCollection(ArrayList::new));
	}

	public AttachmentEntity toAttachmentEntity(final AttachmentData attachmentData) {
		return Optional.ofNullable(attachmentData)
			.map(data -> new AttachmentEntity()
				.withFileName(data.filename())
				.withContentType(data.contentType())
				.withContent(blobUtil.convertToBlob(data.inputStream())))
			.orElse(null);
	}

}
