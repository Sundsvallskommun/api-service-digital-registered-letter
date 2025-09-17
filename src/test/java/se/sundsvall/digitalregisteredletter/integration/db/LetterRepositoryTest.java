package se.sundsvall.digitalregisteredletter.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
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
class LetterRepositoryTest {

	@Autowired
	private LetterRepository letterRepository;

	@Test
	void findByIdAndMunicipalityIdAndDeleted() {
		assertThat(letterRepository.findByIdAndMunicipalityIdAndDeleted("59eeec4c-81f3-4a96-918e-43a5e08a8ef0", "2281", false)).isEmpty();
		assertThat(letterRepository.findByIdAndMunicipalityIdAndDeleted("59eeec4c-81f3-4a96-918e-43a5e08a8ef0", "2260", true)).isEmpty();
		assertThat(letterRepository.findByIdAndMunicipalityIdAndDeleted("59eeec4c-81f3-4a96-918e-43a5e08a8ef0", "2281", true)).isPresent();

		assertThat(letterRepository.findByIdAndMunicipalityIdAndDeleted("450970bb-118c-43a8-8813-6b67c2d33a3b", "2260", false)).isEmpty();
		assertThat(letterRepository.findByIdAndMunicipalityIdAndDeleted("450970bb-118c-43a8-8813-6b67c2d33a3b", "2281", true)).isEmpty();
		assertThat(letterRepository.findByIdAndMunicipalityIdAndDeleted("450970bb-118c-43a8-8813-6b67c2d33a3b", "2260", true)).isPresent();
	}

	@Test
	void findAllByDeleted() {
		assertThat(letterRepository.findAllByDeleted(true))
			.hasSize(2)
			.extracting(LetterEntity::getId).containsExactlyInAnyOrder(
				"59eeec4c-81f3-4a96-918e-43a5e08a8ef0",
				"450970bb-118c-43a8-8813-6b67c2d33a3b");
	}

	@Test
	void findAllByMunicipalityIdAndDeletedFalse() {
		assertThat(letterRepository.findAllByMunicipalityIdAndDeleted("2281", false, PageRequest.of(0, 100)).getContent())
			.hasSize(3)
			.extracting(LetterEntity::getId).containsExactlyInAnyOrder(
				"43a32404-28ee-480f-a095-00d48109afab",
				"f8853893-46a9-4249-a0e5-35d5595efd91",
				"9bb97fd2-4410-4a4b-9019-fdd98f01bd7c");
	}

	@Test
	void findAllByMunicipalityIdAndDeletedTrue() {
		assertThat(letterRepository.findAllByMunicipalityIdAndDeleted("2281", true, PageRequest.of(0, 100)).getContent())
			.hasSize(1)
			.extracting(LetterEntity::getId).containsExactly(
				"59eeec4c-81f3-4a96-918e-43a5e08a8ef0");
	}

	@Test
	void findByIdAndDeleted() {
		assertThat(letterRepository.findByIdAndDeleted("43a32404-28ee-480f-a095-00d48109afab", false)).isPresent(); // Not deleted message
		assertThat(letterRepository.findByIdAndDeleted("43a32404-28ee-480f-a095-00d48109afab", true)).isEmpty(); // Not deleted message (same as above)
		assertThat(letterRepository.findByIdAndDeleted("59eeec4c-81f3-4a96-918e-43a5e08a8ef0", true)).isPresent(); // Deleted message
	}
}
