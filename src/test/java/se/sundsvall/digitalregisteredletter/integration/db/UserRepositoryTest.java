package se.sundsvall.digitalregisteredletter.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@ParameterizedTest
	@ValueSource(strings = {
		"joe01doe", "JOE01DOE"
	})
	void findByUsernameIgnoreCase(String username) {
		final var result = userRepository.findByUsernameIgnoreCase(username);

		assertThat(result).isPresent().hasValueSatisfying(assertedUserEntity -> {
			assertThat(assertedUserEntity.getId()).isEqualTo("3bb3dc98-c674-448a-aa1c-bc4bdf3258bc");
			assertThat(assertedUserEntity.getUsername()).isEqualTo("joe01doe");
			assertThat(assertedUserEntity.getLetters()).hasSize(4).satisfies(letters -> {
				assertThat(letters.stream().map(LetterEntity::getId).toList()).containsExactlyInAnyOrder(
					"43a32404-28ee-480f-a095-00d48109afab",
					"f8853893-46a9-4249-a0e5-35d5595efd91",
					"59eeec4c-81f3-4a96-918e-43a5e08a8ef0",
					"450970bb-118c-43a8-8813-6b67c2d33a3b");
			});
		});
	}

	@Test
	void findByUsernameIgnoreCaseWithNoMatch() {
		assertThat(userRepository.findByUsernameIgnoreCase("not-present")).isEmpty();
	}
}
