package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

class AttachmentsTest {

	@Test
	void attachmentsConstructorTest() {
		var multiPartFile = Mockito.mock(MultipartFile.class);

		var files = List.of(multiPartFile);

		var attachments = new Attachments(files);

		assertThat(attachments).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(attachments.files()).isEqualTo(files);
	}

	@Test
	void attachmentsBuilderTest() {
		var multiPartFile = Mockito.mock(MultipartFile.class);

		var files = List.of(multiPartFile);

		var attachments = AttachmentsBuilder.create()
			.withFiles(files)
			.build();

		assertThat(attachments).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(attachments.files()).isEqualTo(files);
	}
}
