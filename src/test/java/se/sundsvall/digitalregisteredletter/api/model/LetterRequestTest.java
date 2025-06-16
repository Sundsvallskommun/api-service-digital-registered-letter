package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LetterRequestTest {

	@Test
	void letterRequestConstructorTest() {
		var letterRequest = new LetterRequest();

		assertThat(letterRequest).isNotNull().hasNoNullFieldsOrProperties();
	}

	@Test
	void letterRequestBuilderTest() {
		var letterRequest = LetterRequestBuilder.create()
			.build();

		assertThat(letterRequest).isNotNull().hasNoNullFieldsOrProperties();
	}
}
