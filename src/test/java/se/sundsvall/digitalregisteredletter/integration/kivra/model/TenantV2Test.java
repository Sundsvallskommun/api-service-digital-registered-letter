package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantV2Test {

	private static final String NAME = "Department 44";
	private static final String LEGAL_NAME = "Department 44 AB";
	private static final String ORG_NR = "SE559162813601";

	@Test
	void constructorTest() {
		final var companyId = new TenantV2.CompanyId(LEGAL_NAME, ORG_NR);
		final var bean = new TenantV2(NAME, List.of(companyId));

		assertBean(bean);
	}

	@Test
	void builderTest() {
		final var companyId = CompanyIdBuilder.create()
			.withName(LEGAL_NAME)
			.withOrgNr(ORG_NR)
			.build();
		final var bean = TenantV2Builder.create()
			.withName(NAME)
			.withCompanyIds(List.of(companyId))
			.build();

		assertBean(bean);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(new TenantV2(null, null)).hasAllNullFieldsOrProperties();
		assertThat(TenantV2Builder.create().build()).hasAllNullFieldsOrProperties();
		assertThat(CompanyIdBuilder.create().build()).hasAllNullFieldsOrProperties();
	}

	private static void assertBean(final TenantV2 bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.name()).isEqualTo(NAME);
		assertThat(bean.companyIds()).hasSize(1).first().satisfies(companyId -> {
			assertThat(companyId.name()).isEqualTo(LEGAL_NAME);
			assertThat(companyId.orgNr()).isEqualTo(ORG_NR);
		});
	}

}
