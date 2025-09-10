package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrganizationTest {
	private final static String ORGANIZATION_NAME = "organizationName";
	private final static Integer ORGANIZATION_NUMBER = 112;

	@Test
	void constructorTest() {
		final var bean = new Organization(ORGANIZATION_NUMBER, ORGANIZATION_NAME);

		assertBean(bean);
	}

	@Test
	void builderTest() {
		final var bean = OrganizationBuilder.create()
			.withName(ORGANIZATION_NAME)
			.withNumber(ORGANIZATION_NUMBER)
			.build();

		assertBean(bean);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(new Organization(null, null)).hasAllNullFieldsOrProperties();
		assertThat(OrganizationBuilder.create().build()).hasAllNullFieldsOrProperties();
	}

	private static void assertBean(Organization bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.name()).isEqualTo(ORGANIZATION_NAME);
		assertThat(bean.number()).isEqualTo(ORGANIZATION_NUMBER);
	}
}
