package se.sundsvall.digitalregisteredletter.service.mapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.digitalregisteredletter.service.model.AttachmentData;
import se.sundsvall.digitalregisteredletter.service.util.BlobUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentMapperTest {

	@Mock
	private BlobUtil blobUtil;

	@InjectMocks
	private AttachmentMapper attachmentMapper;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(blobUtil);
	}

	@Test
	void toAttachmentEntities() {
		final var blob = Mockito.mock(Blob.class);
		final var inputStream = new ByteArrayInputStream(new byte[0]);
		final var attachmentData = new AttachmentData("file", "application/pdf", inputStream);
		when(blobUtil.convertToBlob(any(InputStream.class))).thenReturn(blob);

		final var attachmentEntities = attachmentMapper.toAttachmentEntities(List.of(attachmentData, attachmentData));

		assertThat(attachmentEntities).isNotNull().isNotEmpty().allSatisfy(attachment -> {
			assertThat(attachment.getFileName()).isEqualTo("file");
			assertThat(attachment.getContentType()).isEqualTo("application/pdf");
			assertThat(attachment.getContent()).isEqualTo(blob);
		});
	}

	@Test
	void toAttachmentEntity() {
		final var blob = Mockito.mock(Blob.class);
		final var inputStream = new ByteArrayInputStream(new byte[0]);
		final var attachmentData = new AttachmentData("file", "application/pdf", inputStream);
		when(blobUtil.convertToBlob(any(InputStream.class))).thenReturn(blob);

		final var attachmentEntity = attachmentMapper.toAttachmentEntity(attachmentData);

		assertThat(attachmentEntity).isNotNull();
		assertThat(attachmentEntity.getFileName()).isEqualTo("file");
		assertThat(attachmentEntity.getContentType()).isEqualTo("application/pdf");
		assertThat(attachmentEntity.getContent()).isEqualTo(blob);
	}

}
