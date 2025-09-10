package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;

class LettersTest {
	private final static List<Letter> LETTER_LIST = List.of(LetterBuilder.create().build());
	private final static PagingAndSortingMetaData META_DATA = PagingAndSortingMetaData.create()
		.withPage(1)
		.withCount(1);

	@Test
	void constructorTest() {
		final var bean = new Letters(META_DATA, LETTER_LIST);

		assertBean(bean);
	}

	@Test
	void builderTest() {
		final var bean = LettersBuilder.create()
			.withLetters(LETTER_LIST)
			.withMetaData(META_DATA)
			.build();

		assertBean(bean);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(new Letters(null, null)).hasAllNullFieldsOrProperties();
		assertThat(LettersBuilder.create().build()).hasAllNullFieldsOrProperties();
	}

	private static void assertBean(Letters letters) {
		assertThat(letters).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(letters.metaData()).isEqualTo(META_DATA);
		assertThat(letters.letters()).isEqualTo(LETTER_LIST);
	}
}
