package se.sundsvall.digitalregisteredletter.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.integration.db.LetterRepository;

@WireMockAppTestSuite(files = "classpath:/SchedulerIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class SchedulerIT extends AbstractAppTest {

	// Id that matches a letter in the test data.
	private static final String LETTER_ID = "43a32404-28ee-480f-a095-00d48109afab";

	@Autowired
	private LetterRepository letterRepository;

	@Test
	void test01_updateLetterStatuses() {
		// Assert that the letter status is "NEW" before the update
		final var letterBeforeUpdate = letterRepository.findById(LETTER_ID).orElseThrow();
		assertThat(letterBeforeUpdate.getStatus()).isEqualTo("NEW");

		setupCall()
			.withServicePath("/2281/scheduler")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();

		// Assert that the letter status is "SIGNED" after the update
		final var letterAfterUpdate = letterRepository.findById(LETTER_ID).orElseThrow();
		assertThat(letterAfterUpdate.getStatus()).isEqualTo("SIGNED");
	}
}
