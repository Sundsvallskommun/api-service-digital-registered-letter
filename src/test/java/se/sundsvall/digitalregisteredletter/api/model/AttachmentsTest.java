package se.sundsvall.digitalregisteredletter.api.model;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class AttachmentsTest {
	private static final List<MultipartFile> MULTIPART_FILES = List.of(Mockito.mock(MultipartFile.class));

	@Test
	void constructorTest() {
		final var bean = new Attachments(MULTIPART_FILES);

		assertBean(bean);
	}

	@Test
	void builderTest() {
		final var bean = AttachmentsBuilder.create()
			.withFiles(MULTIPART_FILES)
			.build();

		assertBean(bean);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(new Attachments(null)).hasAllNullFieldsOrProperties();
		assertThat(AttachmentsBuilder.create().build()).hasAllNullFieldsOrProperties();
	}

	private static void assertBean(Attachments bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.files()).isEqualTo(MULTIPART_FILES);
	}
}
