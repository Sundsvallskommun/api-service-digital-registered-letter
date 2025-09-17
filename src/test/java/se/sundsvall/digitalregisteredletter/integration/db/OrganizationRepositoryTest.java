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

		assertThat(result).isPresent().hasValueSatisfying(assertedOrganizationEntity -> {
			assertThat(assertedOrganizationEntity.getId()).isEqualTo("a95aa330-7cad-4ce1-8bef-1e742fcac6e4");
			assertThat(assertedOrganizationEntity.getName()).isEqualTo("Department 44");
			assertThat(assertedOrganizationEntity.getNumber()).isEqualTo(44);
			assertThat(assertedOrganizationEntity.getLetters()).hasSize(3).satisfies(letters -> {
				assertThat(letters.stream().map(LetterEntity::getId).toList()).containsExactlyInAnyOrder(
					"59eeec4c-81f3-4a96-918e-43a5e08a8ef0",
					"f8853893-46a9-4249-a0e5-35d5595efd91",
					"43a32404-28ee-480f-a095-00d48109afab");
			});
		});
	}

	@Test
	void findByNumberWithNoMatch() {
		assertThat(organizationRepository.findByNumber(911)).isNotPresent();
	}
}
