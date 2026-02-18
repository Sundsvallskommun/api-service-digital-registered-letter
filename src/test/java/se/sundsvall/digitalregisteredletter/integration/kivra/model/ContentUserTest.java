package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentUserTest {

	private static final String SUBJECT = "subject";
	private static final LocalDate GENERATED_AT = LocalDate.now();
	private static final String TYPE = "type";

	@Test
	void testConstructor() {
		var contentUser = new ContentUser(SUBJECT, GENERATED_AT, TYPE);

		assertThat(contentUser.subject()).isEqualTo(SUBJECT);
		assertThat(contentUser.generatedAt()).isEqualTo(GENERATED_AT);
		assertThat(contentUser.type()).isEqualTo(TYPE);
	}

	@Test
	void testBuilder() {
		var contentUser = ContentUserBuilder.create()
			.withSubject(SUBJECT)
			.withGeneratedAt(GENERATED_AT)
			.withType(TYPE)
			.build();

		assertThat(contentUser.subject()).isEqualTo(SUBJECT);
		assertThat(contentUser.generatedAt()).isEqualTo(GENERATED_AT);
		assertThat(contentUser.type()).isEqualTo(TYPE);
	}
}
