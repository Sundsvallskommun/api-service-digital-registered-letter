package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LettersTest {

	@Test
	void lettersConstructorTest() {

	}

	@Test
	void lettersBuilderTest() {
		var letterResponse = LettersBuilder.create()
			.build();

		assertThat(letterResponse).isNotNull().hasAllNullFieldsOrProperties();
	}

}
