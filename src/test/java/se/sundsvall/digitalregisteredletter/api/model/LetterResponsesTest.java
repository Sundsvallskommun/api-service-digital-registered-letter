package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;

class LetterResponsesTest {

	@Test
	void letterResponseConstructorTest() {
		var metaData = new PagingAndSortingMetaData();
		var letters = List.of(new LetterResponse());
		var letterResponses = new LetterResponses(metaData, letters);

		assertThat(letterResponses).isNotNull().hasNoNullFieldsOrProperties();
	}

	@Test
	void letterResponseBuilderTest() {
		var letterResponse = LetterResponsesBuilder.create()
			.build();

		assertThat(letterResponse).isNotNull().hasAllNullFieldsOrProperties();
	}

}
