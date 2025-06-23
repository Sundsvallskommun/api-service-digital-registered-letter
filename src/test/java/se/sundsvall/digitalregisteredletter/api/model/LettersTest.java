package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;

class LettersTest {

	@Test
	void lettersConstructorTest() {
		var letterList = List.of(LetterBuilder.create().build());
		var metaData = PagingAndSortingMetaData.create()
			.withPage(1)
			.withCount(1);

		var letters = new Letters(metaData, letterList);

		assertThat(letters).isNotNull();
		assertThat(letters.metaData()).isEqualTo(metaData);
		assertThat(letters.letters()).isEqualTo(letterList);
	}

	@Test
	void lettersBuilderTest() {
		var letterResponse = LettersBuilder.create()
			.build();

		assertThat(letterResponse).isNotNull().hasAllNullFieldsOrProperties();
	}

}
