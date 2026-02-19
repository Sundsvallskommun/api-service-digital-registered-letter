package se.sundsvall.digitalregisteredletter.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantTest {

	private static final String ID = "cb20c51f-fcf3-42c0-b613-de563634a8ec";
	private static final String ORG_NUMBER = "5591628136";
	private static final String TENANT_KEY = "some-tenant-key";

	@Test
	void constructorTest() {
		final var bean = new Tenant(ID, ORG_NUMBER, TENANT_KEY);

		assertBean(bean);
	}

	@Test
	void builderTest() {
		final var bean = TenantBuilder.create()
			.withId(ID)
			.withOrgNumber(ORG_NUMBER)
			.withTenantKey(TENANT_KEY)
			.build();

		assertBean(bean);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(new Tenant(null, null, null)).hasAllNullFieldsOrProperties();
		assertThat(TenantBuilder.create().build()).hasAllNullFieldsOrProperties();
	}

	private static void assertBean(final Tenant bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.id()).isEqualTo(ID);
		assertThat(bean.orgNumber()).isEqualTo(ORG_NUMBER);
		assertThat(bean.tenantKey()).isEqualTo(TENANT_KEY);
	}
}
