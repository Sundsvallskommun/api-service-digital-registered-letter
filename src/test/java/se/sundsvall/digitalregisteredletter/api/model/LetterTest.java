package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class LetterTest {

	@Test
	void letterConstructorTest() {
		var offsetDateTime = OffsetDateTime.of(2025, 6, 18, 0, 0, 0, 0, OffsetDateTime.now().getOffset());
		var id = "id";
		var municipalityId = "municipalityId";
		var status = "status";
		var body = "body";
		var contentType = "contentType";
		var created = offsetDateTime.minusDays(1);
		var updated = offsetDateTime.minusHours(1);

		var attachment = AttachmentBuilder.create()
			.withId("attachmentId")
			.withFileName("fileName")
			.withContentType("contentType")
			.build();

		var attachments = List.of(attachment);

		var supportInfo = SupportInfoBuilder.create()
			.withSupportText("supportText")
			.withContactInformationUrl("contactInformationUrl")
			.withContactInformationEmail("contactInformationEmail")
			.withContactInformationPhoneNumber("contactInformationPhoneNumber")
			.build();

		var letter = new Letter(id, municipalityId, status, body, contentType, created, updated, supportInfo, attachments);

		assertThat(letter).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(letter.id()).isEqualTo(id);
		assertThat(letter.municipalityId()).isEqualTo(municipalityId);
		assertThat(letter.status()).isEqualTo(status);
		assertThat(letter.body()).isEqualTo(body);
		assertThat(letter.contentType()).isEqualTo(contentType);
		assertThat(letter.created()).isEqualTo(created);
		assertThat(letter.updated()).isEqualTo(updated);
		assertThat(letter.supportInfo()).isEqualTo(supportInfo);
		assertThat(letter.attachments()).isNotNull().containsExactly(attachment);
	}

	@Test
	void letterBuilderTest() {
		var offsetDateTime = OffsetDateTime.of(2025, 6, 18, 0, 0, 0, 0, OffsetDateTime.now().getOffset());
		var id = "id";
		var municipalityId = "municipalityId";
		var status = "status";
		var body = "body";
		var contentType = "contentType";
		var created = offsetDateTime.minusDays(1);
		var updated = offsetDateTime.minusHours(1);

		var attachment = AttachmentBuilder.create()
			.withId("attachmentId")
			.withFileName("fileName")
			.withContentType("contentType")
			.build();

		var attachments = List.of(attachment);

		var supportInfo = SupportInfoBuilder.create()
			.withSupportText("supportText")
			.withContactInformationUrl("contactInformationUrl")
			.withContactInformationEmail("contactInformationEmail")
			.withContactInformationPhoneNumber("contactInformationPhoneNumber")
			.build();

		var letter = LetterBuilder.create()
			.withId(id)
			.withMunicipalityId(municipalityId)
			.withStatus(status)
			.withBody(body)
			.withContentType(contentType)
			.withCreated(created)
			.withUpdated(updated)
			.withAttachments(attachments)
			.withSupportInfo(supportInfo)
			.build();

		assertThat(letter).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(letter.id()).isEqualTo(id);
		assertThat(letter.supportInfo()).isEqualTo(supportInfo);
		assertThat(letter.municipalityId()).isEqualTo(municipalityId);
		assertThat(letter.status()).isEqualTo(status);
		assertThat(letter.body()).isEqualTo(body);
		assertThat(letter.contentType()).isEqualTo(contentType);
		assertThat(letter.created()).isEqualTo(created);
		assertThat(letter.updated()).isEqualTo(updated);
		assertThat(letter.attachments()).isNotNull().containsExactly(attachment);
	}
}
