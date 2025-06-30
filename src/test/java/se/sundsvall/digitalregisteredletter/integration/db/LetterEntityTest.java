package se.sundsvall.digitalregisteredletter.integration.db;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static se.sundsvall.TestDataFactory.createAttachmentEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LetterEntityTest {

	@BeforeAll
	static void setup() {
		final var random = new Random();
		registerValueGenerator(() -> now().plusDays(random.nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		org.hamcrest.MatcherAssert.assertThat(LetterEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		var offsetDateTime = OffsetDateTime.of(2025, 6, 18, 0, 0, 0, 0, OffsetDateTime.now().getOffset());
		var id = "id";
		var municipalityId = "1234";
		var created = offsetDateTime.minusDays(1);
		var updated = offsetDateTime.minusHours(1);
		var body = "body";
		var subject = "subject";
		var partyId = "partyId";
		var contentType = "text/plain";
		var status = "status";
		var deleted = true;
		var attachments = List.of(createAttachmentEntity());
		var supportInfo = SupportInfo.create()
			.withSupportText("support text")
			.withContactInformationUrl("https://example.com/contact")
			.withContactInformationEmail("support@email.com")
			.withContactInformationPhoneNumber("+46123456789");

		var letterEntity = LetterEntity.create()
			.withId(id)
			.withMunicipalityId(municipalityId)
			.withCreated(created)
			.withUpdated(updated)
			.withBody(body)
			.withContentType(contentType)
			.withStatus(status)
			.withDeleted(deleted)
			.withAttachments(attachments)
			.withPartyId(partyId)
			.withSubject(subject)
			.withSupportInfo(supportInfo);

		assertThat(letterEntity.getAttachments()).isEqualTo(attachments);
		assertThat(letterEntity.getId()).isEqualTo(id);
		assertThat(letterEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(letterEntity.getCreated()).isEqualTo(created);
		assertThat(letterEntity.getUpdated()).isEqualTo(updated);
		assertThat(letterEntity.getBody()).isEqualTo(body);
		assertThat(letterEntity.getContentType()).isEqualTo(contentType);
		assertThat(letterEntity.getStatus()).isEqualTo(status);
		assertThat(letterEntity.isDeleted()).isEqualTo(deleted);
		assertThat(letterEntity.getSupportInfo()).isEqualTo(supportInfo);
		assertThat(letterEntity.getPartyId()).isEqualTo(partyId);
		assertThat(letterEntity.getSubject()).isEqualTo(subject);

		assertThat(letterEntity).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(LetterEntity.create()).hasAllNullFieldsOrPropertiesExcept("attachments", "deleted");
		assertThat(new LetterEntity()).hasAllNullFieldsOrPropertiesExcept("attachments", "deleted");
	}

}
