package se.sundsvall.digitalregisteredletter.integration.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class AttachmentRepositoryTest {

	@Autowired
	private AttachmentRepository attachmentRepository;

	@Test
	void findByIdAndLetterIdAndLetter_MunicipalityId() {
		assertThat(attachmentRepository.findByIdAndLetterIdAndLetter_MunicipalityId("f4666ea6-0324-490f-8e27-2b704e580a0a", "43a32404-28ee-480f-a095-00d48109afab", "2281")).isPresent();
	}
}
