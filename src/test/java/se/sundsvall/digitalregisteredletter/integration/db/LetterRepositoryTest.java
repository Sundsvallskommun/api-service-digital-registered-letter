package se.sundsvall.digitalregisteredletter.integration.db;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilterBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

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

	@BeforeAll
	static void setup() {
		RequestId.init();
	}

	@Test
	void persistLetter() {
		final var letterEntity = letterRepository.save(LetterEntity.create()
			.withMunicipalityId("2262"));

		assertThat(letterEntity.getRequestId()).isEqualTo(RequestId.get());
		assertThat(letterEntity.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(letterEntity.getUpdated()).isEqualTo(letterEntity.getCreated());
	}

	@Test
	void updateLetter() {
		final var letterEntity = letterRepository.getReferenceById("1a7b65d7-bafd-49be-9e97-6406b1bf5886");
		final var initialRequestId = "03ae04dc-ed22-4958-a1af-70e496e02fa8";

		assertThat(letterEntity.getCreated()).isEqualTo(letterEntity.getUpdated());
		assertThat(letterEntity.getRequestId()).isEqualTo(initialRequestId);

		letterRepository.saveAndFlush(letterEntity.withStatus("modified"));

		assertThat(letterEntity.getUpdated()).isAfter(letterEntity.getCreated());
		assertThat(letterEntity.getRequestId()).isEqualTo(RequestId.get());
	}

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
	void findAllByMunicipalityIdAndIdInAndDeletedFalse() {
		final var municipalityId = "2281";
		final var letterIds = List.of(
			"43a32404-28ee-480f-a095-00d48109afab",
			"f8853893-46a9-4249-a0e5-35d5595efd91");

		assertThat(letterRepository.findAllByMunicipalityIdAndIdInAndDeletedFalse(municipalityId, letterIds))
			.hasSize(2)
			.extracting(LetterEntity::getId)
			.containsExactlyInAnyOrderElementsOf(letterIds);
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
	void findAllByMunicipalityIdAndDeleted() {
		assertThat(letterRepository.findAllByFilter("2281", LetterFilterBuilder.create().build(), false, PageRequest.of(0, 100)).getContent())
			.hasSize(3)
			.extracting(LetterEntity::getId).containsExactlyInAnyOrder(
				"43a32404-28ee-480f-a095-00d48109afab",
				"f8853893-46a9-4249-a0e5-35d5595efd91",
				"9bb97fd2-4410-4a4b-9019-fdd98f01bd7c");
	}

	@Test
	void findAllByMunicipalityIdAndDeletedTrue() {
		assertThat(letterRepository.findAllByFilter("2281", LetterFilterBuilder.create().build(), true, PageRequest.of(0, 100)).getContent())
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

	@Test
	void findByIdAndVerifySigningInfo() {
		assertThat(letterRepository.findById("1a7b65d7-bafd-49be-9e97-6406b1bf5886")).isPresent().hasValueSatisfying(entity -> {
			assertThat(entity.getStatus()).isEqualTo("PENDING");
			assertThat(entity.getSigningInformation()).isNull();
		});
		assertThat(letterRepository.findById("f8853893-46a9-4249-a0e5-35d5595efd91")).isPresent().hasValueSatisfying(entity -> {
			assertThat(entity.getStatus()).isEqualTo("SIGNED");
			assertThat(entity.getSigningInformation()).isNotNull().satisfies(signingInfo -> {
				assertThat(signingInfo.getStatus()).isEqualTo("COMPLETED");
			});
		});
	}
}
