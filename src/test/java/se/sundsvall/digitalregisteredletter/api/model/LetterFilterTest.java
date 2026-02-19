package se.sundsvall.digitalregisteredletter.api.model;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LetterFilterTest {
	private static final OffsetDateTime CREATED_EARLIEST = OffsetDateTime.MIN;
	private static final OffsetDateTime CREATED_LATEST = OffsetDateTime.MAX;
	private static final Integer ORG_ID = 44;
	private static final String USERNAME = "username";

	@Test
	void constructorTest() {
		final var bean = new LetterFilter(ORG_ID, USERNAME, CREATED_EARLIEST, CREATED_LATEST);

		assertBean(bean);
	}

	@Test
	void builderTest() {
		final var bean = LetterFilterBuilder.create()
			.withCreatedEarliest(CREATED_EARLIEST)
			.withCreatedLatest(CREATED_LATEST)
			.withOrgId(ORG_ID)
			.withUsername(USERNAME)
			.build();

		assertBean(bean);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(new LetterFilter(null, null, null, null)).hasAllNullFieldsOrProperties();
		assertThat(LetterFilterBuilder.create().build()).hasAllNullFieldsOrProperties();
	}

	private static void assertBean(LetterFilter bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.createdEarliest()).isEqualTo(CREATED_EARLIEST);
		assertThat(bean.createdLatest()).isEqualTo(CREATED_LATEST);
		assertThat(bean.orgId()).isEqualTo(ORG_ID);
		assertThat(bean.username()).isEqualTo(USERNAME);
	}
}
