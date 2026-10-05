package se.sundsvall.digitalregisteredletter.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KivraTenantRequestTest {
	private static final String NAME = "Department 44";
	private static final String LEGAL_NAME = "Department 44 AB";
	private static final String ORG_NUMBER = "5591628136";

	@Test
	void constructorTest() {
		final var bean = new KivraTenantRequest(NAME, LEGAL_NAME, ORG_NUMBER);

		assertBean(bean);
	}

	@Test
	void builderTest() {
		final var bean = KivraTenantRequestBuilder.create()
			.withName(NAME)
			.withLegalName(LEGAL_NAME)
			.withOrgNumber(ORG_NUMBER)
			.build();

		assertBean(bean);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(new KivraTenantRequest(null, null, null)).hasAllNullFieldsOrProperties();
		assertThat(KivraTenantRequestBuilder.create().build()).hasAllNullFieldsOrProperties();
	}

	private static void assertBean(final KivraTenantRequest bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.name()).isEqualTo(NAME);
		assertThat(bean.legalName()).isEqualTo(LEGAL_NAME);
		assertThat(bean.orgNumber()).isEqualTo(ORG_NUMBER);
	}
}
