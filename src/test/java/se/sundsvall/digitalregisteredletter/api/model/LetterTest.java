package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.digitalregisteredletter.api.model.Letter.Attachment;

class LetterTest {
	private final static OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(2025, 6, 18, 0, 0, 0, 0, OffsetDateTime.now().getOffset());
	private final static String ID = "id";
	private final static String MUNICIPALITY_ID = "municipalityId";
	private final static String STATUS = "status";
	private final static String BODY = "body";
	private final static String CONTENT_TYPE = "contentType";
	private final static OffsetDateTime CREATED = OFFSET_DATE_TIME.minusDays(1);
	private final static OffsetDateTime UPDATED = OFFSET_DATE_TIME.minusHours(1);
	private final static List<Attachment> ATTACHMENTS = List.of(AttachmentBuilder.create()
		.withId("attachmentId")
		.withFileName("fileName")
		.withContentType("contentType")
		.build());
	private static final SupportInfo SUPPORT_INFO = SupportInfoBuilder.create()
		.withSupportText("supportText")
		.withContactInformationUrl("contactInformationUrl")
		.withContactInformationEmail("contactInformationEmail")
		.withContactInformationPhoneNumber("contactInformationPhoneNumber")
		.build();

	@Test
	void constructorTest() {
		final var bean = new Letter(ID, MUNICIPALITY_ID, STATUS, BODY, CONTENT_TYPE, CREATED, UPDATED, SUPPORT_INFO, ATTACHMENTS);

		assertBean(bean);
	}

	@Test
	void builderTest() {
		final var bean = LetterBuilder.create()
			.withId(ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withStatus(STATUS)
			.withBody(BODY)
			.withContentType(CONTENT_TYPE)
			.withCreated(CREATED)
			.withUpdated(UPDATED)
			.withAttachments(ATTACHMENTS)
			.withSupportInfo(SUPPORT_INFO)
			.build();

		assertBean(bean);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(new Letter(null, null, null, null, null, null, null, null, null)).hasAllNullFieldsOrProperties();
		assertThat(LetterBuilder.create().build()).hasAllNullFieldsOrProperties();
	}

	private static void assertBean(Letter letter) {
		assertThat(letter).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(letter.id()).isEqualTo(ID);
		assertThat(letter.supportInfo()).isEqualTo(SUPPORT_INFO);
		assertThat(letter.municipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(letter.status()).isEqualTo(STATUS);
		assertThat(letter.body()).isEqualTo(BODY);
		assertThat(letter.contentType()).isEqualTo(CONTENT_TYPE);
		assertThat(letter.created()).isEqualTo(CREATED);
		assertThat(letter.updated()).isEqualTo(UPDATED);
		assertThat(letter.attachments()).usingRecursiveComparison().isEqualTo(ATTACHMENTS);
	}
}
