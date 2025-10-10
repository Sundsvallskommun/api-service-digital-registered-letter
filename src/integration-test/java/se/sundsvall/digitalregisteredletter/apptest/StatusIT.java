package se.sundsvall.digitalregisteredletter.apptest;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.digitalregisteredletter.Application;

@WireMockAppTestSuite(files = "classpath:/StatusIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class StatusIT extends AbstractAppTest {

	private static final String SERVICE_PATH = "/2281/status/letters";
	private static final String RESPONSE = "response.json";
	private static final String REQUEST = "request.json";

	@Test
	void test01_getStatuses() {
		setupCall()
			.withServicePath(SERVICE_PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getStatuses_duplicates() {
		setupCall()
			.withServicePath(SERVICE_PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getStatuses_softDeletedNotFound() {
		setupCall()
			.withServicePath(SERVICE_PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_validation_emptyList() {
		setupCall()
			.withServicePath(SERVICE_PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_validation_invalidUuid() {
		setupCall()
			.withServicePath(SERVICE_PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}
}
