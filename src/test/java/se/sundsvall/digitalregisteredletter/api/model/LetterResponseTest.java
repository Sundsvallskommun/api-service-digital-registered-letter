package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LetterResponseTest {

	@Test
	void letterResponseConstructorTest() {
		var letterResponse = new LetterResponse();

		assertThat(letterResponse).isNotNull().hasNoNullFieldsOrProperties();
	}

	@Test
	void letterResponseBuilderTest() {
		var letterResponse = LetterResponseBuilder.create()
			.build();

		assertThat(letterResponse).isNotNull().hasNoNullFieldsOrProperties();
	}
}
