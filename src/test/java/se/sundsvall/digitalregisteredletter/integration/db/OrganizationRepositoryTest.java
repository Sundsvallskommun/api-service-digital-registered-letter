package se.sundsvall.digitalregisteredletter.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import org.junit.jupiter.api.Test;
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
class OrganizationRepositoryTest {

	@Autowired
	private OrganizationRepository organizationRepository;

	@Test
	void findByNumber() {
		final var result = organizationRepository.findByNumber(44);

		assertThat(result).isPresent().hasValueSatisfying(oe -> {
			assertThat(oe.getId()).isEqualTo("a95aa330-7cad-4ce1-8bef-1e742fcac6e4");
			assertThat(oe.getName()).isEqualTo("Department 44");
			assertThat(oe.getNumber()).isEqualTo(44);
			assertThat(oe.getLetters()).hasSize(2).satisfies(letters -> {
				assertThat(letters.stream().map(LetterEntity::getId).toList()).containsExactlyInAnyOrder(
					"43a32404-28ee-480f-a095-00d48109afab",
					"f8853893-46a9-4249-a0e5-35d5595efd91");
			});
		});
	}

	@Test
	void findByNumberWithNoMatch() {
		assertThat(organizationRepository.findByNumber(911)).isNotPresent();
	}
}
