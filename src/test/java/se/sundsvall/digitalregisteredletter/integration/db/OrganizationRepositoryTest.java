package se.sundsvall.digitalregisteredletter.integration.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

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
			assertThat(assertedOrganizationEntity.getLetters()).hasSize(4).satisfies(letters -> {
				assertThat(letters.stream().map(LetterEntity::getId).toList()).containsExactlyInAnyOrder(
					"59eeec4c-81f3-4a96-918e-43a5e08a8ef0",
					"f8853893-46a9-4249-a0e5-35d5595efd91",
					"43a32404-28ee-480f-a095-00d48109afab",
					"1a7b65d7-bafd-49be-9e97-6406b1bf5886");
			});
		});
	}

	@Test
	void findByNumberWithNoMatch() {
		assertThat(organizationRepository.findByNumber(911)).isNotPresent();
	}
}
