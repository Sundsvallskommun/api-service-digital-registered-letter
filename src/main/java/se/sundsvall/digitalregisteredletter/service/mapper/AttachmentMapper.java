package se.sundsvall.digitalregisteredletter.service.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.service.util.BlobUtil;

import static java.util.Collections.emptyList;

@Component
public final class AttachmentMapper {

	private final BlobUtil blobUtil;

	public AttachmentMapper(final BlobUtil blobUtil) {
		this.blobUtil = blobUtil;
	}

	public List<AttachmentEntity> toAttachmentEntities(final List<MultipartFile> attachments) {
		return Optional.ofNullable(attachments).orElse(emptyList()).stream()
			.map(this::toAttachmentEntity)
			.collect(Collectors.toCollection(ArrayList::new));
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
