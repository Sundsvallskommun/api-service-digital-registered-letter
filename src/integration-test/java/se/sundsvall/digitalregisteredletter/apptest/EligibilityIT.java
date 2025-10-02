package se.sundsvall.digitalregisteredletter.apptest;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.digitalregisteredletter.Application;

@WireMockAppTestSuite(files = "classpath:/EligibilityIT/", classes = Application.class)
class EligibilityIT extends AbstractAppTest {

	private static final String RESPONSE = "response.json";
	private static final String REQUEST = "request.json";

	@Test
	void test01_allPartyIdsEligible() {
		setupCall()
			.withServicePath(uriBuilder -> uriBuilder.replacePath("/2281/eligibility/kivra").build())
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_somePartyIdsEligible() {
		setupCall()
			.withServicePath(uriBuilder -> uriBuilder.replacePath("/2281/eligibility/kivra").build())
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_noPartyIdsEligible() {
		setupCall()
			.withServicePath(uriBuilder -> uriBuilder.replacePath("/2281/eligibility/kivra").build())
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

}
