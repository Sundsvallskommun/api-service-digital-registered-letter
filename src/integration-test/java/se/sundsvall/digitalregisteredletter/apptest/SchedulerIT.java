package se.sundsvall.digitalregisteredletter.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import java.time.Instant;
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
import se.sundsvall.digitalregisteredletter.integration.db.TenantRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.SigningInformationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.TenantEntity;
import se.sundsvall.digitalregisteredletter.service.util.EncryptionUtility;

@WireMockAppTestSuite(files = "classpath:/SchedulerIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class SchedulerIT extends AbstractAppTest {

	// ID that matches a letter in the test data.
	private static final String LETTER_ID = "9bb97fd2-4410-4a4b-9019-fdd98f01bd7c";

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private LetterRepository letterRepository;

	@Autowired
	private TenantRepository tenantRepository;

	@Autowired
	private EncryptionUtility encryptionUtility;

	private TransactionTemplate transactionTemplate;

	@BeforeEach
	void setUp() {
		transactionTemplate = new TransactionTemplate(transactionManager);

		// Create a tenant with an encrypted key that decrypts to "some-tenant-key"
		// and link it to the letter
		transactionTemplate.executeWithoutResult(status -> {
			final var tenant = tenantRepository.save(TenantEntity.create()
				.withOrgNumber("5566778899")
				.withMunicipalityId("2281")
				.withTenantKey(encryptionUtility.encrypt("some-tenant-key".getBytes())));

			final var letter = letterRepository.findById(LETTER_ID).orElseThrow();
			letter.setStatus("SENT");
			letter.setTenant(tenant);
			letter.setSigningInformation(SigningInformationEntity.create().withStatus("PENDING"));
			letterRepository.save(letter);
		});
	}

	@Test
	void test01_updateLetterStatuses() {
		// Assert that the letter status is "SENT" and has signing information with status "PENDING" before the update
		transactionTemplate.executeWithoutResult(status -> {
			final var letterBeforeUpdate = letterRepository.findById(LETTER_ID).orElseThrow();
			assertThat(letterBeforeUpdate.getStatus()).isEqualTo("SENT");
			assertThat(letterBeforeUpdate.getSigningInformation()).isNotNull();
			assertThat(letterBeforeUpdate.getSigningInformation().getStatus()).isEqualTo("PENDING");
		});

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
				assertThat(signingInformation.getSigned().toInstant()).isEqualTo(Instant.parse("2023-10-01T12:00:00Z"));
			});
		});
	}
}
