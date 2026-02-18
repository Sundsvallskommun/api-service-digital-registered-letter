package se.sundsvall.digitalregisteredletter.integration.db.model;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.hamcrest.core.AllOf.allOf;

class OrganizationEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LetterEntity.create().withId(UUID.randomUUID().toString()), LetterEntity.class);
	}

	@Test
	void testBean() {
		org.hamcrest.MatcherAssert.assertThat(OrganizationEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var id = UUID.randomUUID().toString();
		final var letters = List.of(LetterEntity.create());
		final var name = "name";
		final var number = 123456;

		final var bean = OrganizationEntity.create()
			.withId(id)
			.withLetters(letters)
			.withName(name)
			.withNumber(number);

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getLetters()).isEqualTo(letters);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getNumber()).isEqualTo(number);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(OrganizationEntity.create()).hasAllNullFieldsOrPropertiesExcept("letters").extracting(OrganizationEntity::getLetters).asInstanceOf(LIST).isEmpty();
		assertThat(new OrganizationEntity()).hasAllNullFieldsOrPropertiesExcept("letters").extracting(OrganizationEntity::getLetters).asInstanceOf(LIST).isEmpty();
	}
}
