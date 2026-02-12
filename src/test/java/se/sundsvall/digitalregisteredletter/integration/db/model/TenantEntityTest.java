package se.sundsvall.digitalregisteredletter.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantEntityTest {

	@Test
	void testBean() {
		org.hamcrest.MatcherAssert.assertThat(TenantEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var id = UUID.randomUUID().toString();
		final var orgNumber = "5591628136";
		final var tenantKey = "some-tenant-key";
		final var municipalityId = "2281";

		final var bean = TenantEntity.create()
			.withId(id)
			.withOrgNumber(orgNumber)
			.withTenantKey(tenantKey)
			.withMunicipalityId(municipalityId);

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getOrgNumber()).isEqualTo(orgNumber);
		assertThat(bean.getTenantKey()).isEqualTo(tenantKey);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(TenantEntity.create()).hasAllNullFieldsOrProperties();
		assertThat(new TenantEntity()).hasAllNullFieldsOrProperties();
	}
}
