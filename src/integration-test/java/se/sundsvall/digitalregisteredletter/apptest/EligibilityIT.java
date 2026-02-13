package se.sundsvall.digitalregisteredletter.apptest;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.integration.db.TenantRepository;
import se.sundsvall.digitalregisteredletter.service.util.EncryptionUtility;

@WireMockAppTestSuite(files = "classpath:/EligibilityIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class EligibilityIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ORGANIZATION_NUMBER = "1234567890";
	private static final String RESPONSE = "response.json";
	private static final String REQUEST = "request.json";

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private TenantRepository tenantRepository;

	@Autowired
	private EncryptionUtility encryptionUtility;

	@BeforeEach
	void setUp() {
		final var transactionTemplate = new TransactionTemplate(transactionManager);

		// Update the existing tenant's key to a properly encrypted value that decrypts to "some-tenant-key"
		transactionTemplate.executeWithoutResult(_ -> {
			final var tenant = tenantRepository.findByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER).orElseThrow();
			tenant.setTenantKey(encryptionUtility.encrypt("some-tenant-key".getBytes()));
			tenantRepository.save(tenant);
		});
	}

	@Test
	void test01_allPartyIdsEligible() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/" + ORGANIZATION_NUMBER + "/eligibility/kivra")
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_somePartyIdsEligible() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/" + ORGANIZATION_NUMBER + "/eligibility/kivra")
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_noPartyIdsEligible() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/" + ORGANIZATION_NUMBER + "/eligibility/kivra")
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

}
