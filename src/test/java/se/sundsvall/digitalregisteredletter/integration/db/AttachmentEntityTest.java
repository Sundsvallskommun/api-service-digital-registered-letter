package se.sundsvall.digitalregisteredletter.integration.db;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import java.sql.Blob;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AttachmentEntityTest {

	@Test
	void testBean() {
		org.hamcrest.MatcherAssert.assertThat(AttachmentEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		var id = "id";
		var fileName = "fileName";
		var contentType = "contentType";
		var content = Mockito.mock(Blob.class);

		var attachmentEntity = AttachmentEntity.create()
			.withId(id)
			.withFileName(fileName)
			.withContentType(contentType)
			.withContent(content);

		assertThat(attachmentEntity.getId()).isEqualTo(id);
		assertThat(attachmentEntity.getFileName()).isEqualTo(fileName);
		assertThat(attachmentEntity.getContentType()).isEqualTo(contentType);
		assertThat(attachmentEntity.getContent()).isEqualTo(content);

		assertThat(attachmentEntity).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AttachmentEntity.create()).hasAllNullFieldsOrPropertiesExcept();
		assertThat(new AttachmentEntity()).hasAllNullFieldsOrPropertiesExcept();
	}

}
