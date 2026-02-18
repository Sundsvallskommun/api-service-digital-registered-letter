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

class UserEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LetterEntity.create().withId(UUID.randomUUID().toString()), LetterEntity.class);
	}

	@Test
	void testBean() {
		org.hamcrest.MatcherAssert.assertThat(UserEntity.class, allOf(
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
		final var username = "username";

		final var bean = UserEntity.create()
			.withId(id)
			.withLetters(letters)
			.withUsername(username);

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getLetters()).isEqualTo(letters);
		assertThat(bean.getUsername()).isEqualTo(username);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(UserEntity.create()).hasAllNullFieldsOrPropertiesExcept("letters").extracting(UserEntity::getLetters).asInstanceOf(LIST).isEmpty();
		assertThat(new UserEntity()).hasAllNullFieldsOrPropertiesExcept("letters").extracting(UserEntity::getLetters).asInstanceOf(LIST).isEmpty();
	}
}
