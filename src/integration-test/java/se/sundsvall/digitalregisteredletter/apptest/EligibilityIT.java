package se.sundsvall.digitalregisteredletter.apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.digitalregisteredletter.Application;

@WireMockAppTestSuite(files = "classpath:/EligibilityIT/", classes = Application.class)
class EligibilityIT extends AbstractAppTest {

	private static final String RESPONSE = "response.json";

	private final String partyId1 = "123e4567-e89b-12d3-a456-426614174000";
	private final String partyId2 = "123e4567-e89b-12d3-a456-426614174001";
	private final String partyId3 = "123e4567-e89b-12d3-a456-426614174002";

	private final List<String> partyIds = List.of(partyId1, partyId2, partyId3);

	@Test
	void test01_allPartyIdsEligible() {
		setupCall()
			.withServicePath(uriBuilder -> uriBuilder.replacePath("/2281/eligibility/kivra")
				.queryParam("partyIds", partyIds).build())
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_somePartyIdsEligible() {
		setupCall()
			.withServicePath(uriBuilder -> uriBuilder.replacePath("/2281/eligibility/kivra")
				.queryParam("partyIds", partyIds).build())
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_noPartyIdsEligible() {
		setupCall()
			.withServicePath(uriBuilder -> uriBuilder.replacePath("/2281/eligibility/kivra")
				.queryParam("partyIds", partyIds).build())
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

}
