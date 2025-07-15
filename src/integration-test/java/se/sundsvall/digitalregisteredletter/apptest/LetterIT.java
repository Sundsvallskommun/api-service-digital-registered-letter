package se.sundsvall.digitalregisteredletter.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import java.io.FileNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.integration.db.dao.LetterRepository;

@WireMockAppTestSuite(files = "classpath:/LetterIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/test-data.sql"
})
class LetterIT extends AbstractAppTest {

	private static final String RESPONSE = "response.json";
	private static final String REQUEST = "request.json";

	@Autowired
	private LetterRepository letterRepository;

	@Test
	void test01_getLetter() {
		setupCall()
			.withServicePath("/2281/letters/43a32404-28ee-480f-a095-00d48109afab")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getLetters() {
		setupCall()
			.withServicePath("/2281/letters?page=0&size=10")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_sendLetter() throws FileNotFoundException {
		var lettersBefore = letterRepository.findAll();
		assertThat(lettersBefore).hasSize(2);

		var headers = setupCall()
			.withServicePath("/2281/letters")
			.withHttpMethod(POST)
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("letter", REQUEST)
			.withRequestFile("letterAttachments", "test.pdf")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("/2281/letters/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
			.sendRequest()
			.getResponseHeaders();

		assertThat(headers.get(LOCATION)).isNotNull();

		setupCall()
			.withServicePath(headers.get(LOCATION).getFirst())
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();

		var lettersAfter = letterRepository.findAll();
		assertThat(lettersAfter).hasSize(3);
	}

}
