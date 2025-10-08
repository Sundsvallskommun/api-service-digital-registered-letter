package se.sundsvall.digitalregisteredletter.apptest;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
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

	// ID that matches a letter in the test data.
	private static final String LETTER_ID = "43a32404-28ee-480f-a095-00d48109afab";

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private LetterRepository letterRepository;

	private TransactionTemplate transactionTemplate;

	@BeforeEach
	void setUp() {
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Test
	void test01_updateLetterStatuses() {
		// Assert that the letter status is "NEW" and has no signing information before the update
		final var letterBeforeUpdate = letterRepository.findById(LETTER_ID).orElseThrow();
		assertThat(letterBeforeUpdate.getStatus()).isEqualTo("NEW");
		assertThat(letterBeforeUpdate.getSigningInformation()).isNull();

		setupCall()
			.withServicePath("/2281/scheduler")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();

		// Assert that the letter status is "SIGNED" and that it has signing information after the update
		transactionTemplate.executeWithoutResult(status -> {
			final var letterAfterUpdate = letterRepository.findById(LETTER_ID).orElseThrow();
			assertThat(letterAfterUpdate.getStatus()).isEqualTo("SIGNED");
			assertThat(letterAfterUpdate.getSigningInformation()).isNotNull().satisfies(signingInformation -> {
				assertThat(signingInformation.getContentKey()).isEqualTo("c33e857c-4341-4f54-9044-cdeee56760a8");
				assertThat(signingInformation.getGivenName()).isEqualTo("Joe");
				assertThat(signingInformation.getInternalId()).isEqualTo(LETTER_ID);
				assertThat(signingInformation.getIpAddress()).isEqualTo("127.0.0.1");
				assertThat(signingInformation.getMrtd()).isFalse();
				assertThat(signingInformation.getName()).isEqualTo("Joe Doe");
				assertThat(signingInformation.getOcspResponse()).isEqualTo("MIIHdgoBAKCCB28wggdrBg");
				assertThat(signingInformation.getOrderRef()).isEqualTo("be7c5362-7147-47e3-85d6-f358ccec5ca8");
				assertThat(signingInformation.getPersonalNumber()).isEqualTo("190001011234");
				assertThat(signingInformation.getSignature()).isEqualTo("PD94bWwgdmVyc2lvb");
				assertThat(signingInformation.getStatus()).isEqualTo("COMPLETED");
				assertThat(signingInformation.getSurname()).isEqualTo("Doe");
				assertThat(signingInformation.getSigned()).isEqualTo(OffsetDateTime.of(2023, 10, 01, 14, 00, 00, 000, now().getOffset()));
			});
		});
	}
}
