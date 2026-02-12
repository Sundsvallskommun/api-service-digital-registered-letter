package se.sundsvall.digitalregisteredletter.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TenantEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		org.hamcrest.MatcherAssert.assertThat(TenantEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCodeExcluding("letters"),
			hasValidBeanEqualsExcluding("letters"),
			hasValidBeanToStringExcluding("letters")));
	}

	@Test
	void testBuilderMethods() {
		final var id = UUID.randomUUID().toString();
		final var orgNumber = "5591628136";
		final var tenantKey = "some-tenant-key";
		final var municipalityId = "2281";
		final var created = now();
		final var modified = now();
		final var letters = List.of(LetterEntity.create());

		final var bean = TenantEntity.create()
			.withId(id)
			.withOrgNumber(orgNumber)
			.withTenantKey(tenantKey)
			.withMunicipalityId(municipalityId)
			.withCreated(created)
			.withModified(modified)
			.withLetters(letters);

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getOrgNumber()).isEqualTo(orgNumber);
		assertThat(bean.getTenantKey()).isEqualTo(tenantKey);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getModified()).isEqualTo(modified);
		assertThat(bean.getLetters()).isEqualTo(letters);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(TenantEntity.create()).hasAllNullFieldsOrProperties();
		assertThat(new TenantEntity()).hasAllNullFieldsOrProperties();
	}
}
