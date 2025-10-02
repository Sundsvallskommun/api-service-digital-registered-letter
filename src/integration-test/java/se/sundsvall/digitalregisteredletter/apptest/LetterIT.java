package se.sundsvall.digitalregisteredletter.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

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
import se.sundsvall.digitalregisteredletter.integration.db.OrganizationRepository;
import se.sundsvall.digitalregisteredletter.integration.db.UserRepository;

@WireMockAppTestSuite(files = "classpath:/LetterIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class LetterIT extends AbstractAppTest {

	public static final String HEADER_X_SENT_BY = "X-Sent-By";
	private static final String RESPONSE = "response.json";
	private static final String REQUEST = "request.json";

	@Autowired
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@Autowired
	private LetterRepository letterRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

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
	void test02_getLettersPaged() {
		setupCall()
			.withServicePath("/2281/letters?page=1&size=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_sendLetter() throws FileNotFoundException {
		final var initialLetterSize = transactionTemplate.execute(status -> {
			assertThat(organizationRepository.findByNumber(44)).isPresent().hasValueSatisfying(entity -> assertThat(entity.getLetters()).hasSize(3));
			assertThat(userRepository.findByUsernameIgnoreCase("joe01doe")).isPresent().hasValueSatisfying(entity -> assertThat(entity.getLetters()).hasSize(7));
			return letterRepository.count();
		});

		final var headers = setupCall()
			.withServicePath("/2281/letters")
			.withHttpMethod(POST)
			.withHeader(HEADER_X_SENT_BY, "joe01doe; type=adAccount")
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("letter", REQUEST)
			.withRequestFile("letterAttachments", "test.pdf")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("/2281/letters/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.getResponseHeaders();

		assertThat(headers.get(LOCATION)).isNotNull();

		transactionTemplate.executeWithoutResult(status -> {
			assertThat(letterRepository.count()).isEqualTo(initialLetterSize + 1); // Count should have grown with one (the newly successfully sent letter)
			assertThat(organizationRepository.findByNumber(44)).isPresent().hasValueSatisfying(entity -> assertThat(entity.getLetters()).hasSize(4));
			assertThat(userRepository.findByUsernameIgnoreCase("joe01doe")).isPresent().hasValueSatisfying(entity -> assertThat(entity.getLetters()).hasSize(8));
		});
	}

	@Test
	void test04_sendLetterKivraReturnClientError() throws FileNotFoundException {
		final var initialLetterSize = letterRepository.count();

		final var headers = setupCall()
			.withServicePath("/2281/letters")
			.withHttpMethod(POST)
			.withHeader(HEADER_X_SENT_BY, "joe01doe; type=adAccount")
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("letter", REQUEST)
			.withRequestFile("letterAttachments", "test.pdf")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("/2281/letters/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.getResponseHeaders();

		assertThat(headers.get(LOCATION)).isNotNull();

		assertThat(letterRepository.count()).isEqualTo(initialLetterSize + 1); // Count should have grown with one (the newly failed sent letter)
	}

	@Test
	void test05_sendLetterKivraReturnServerError() throws FileNotFoundException {
		final var initialLetterSize = letterRepository.count();

		final var headers = setupCall()
			.withServicePath("/2281/letters")
			.withHttpMethod(POST)
			.withHeader(HEADER_X_SENT_BY, "joe01doe; type=adAccount")
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("letter", REQUEST)
			.withRequestFile("letterAttachments", "test.pdf")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("/2281/letters/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.getResponseHeaders();

		assertThat(headers.get(LOCATION)).isNotNull();

		assertThat(letterRepository.count()).isEqualTo(initialLetterSize + 1); // Count should have grown with one (the newly failed sent letter)
	}

	@Test
	void test06_getLettersFilteredOnUsername() {
		setupCall()
			.withServicePath("/2281/letters?username=joe01doe&page=0&size=10")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_getLettersFilteredOnDepartmentIdAndDate() {
		setupCall()
			.withServicePath("/2281/letters?orgId=45&createdEarliest=2023-10-01&createdLatest=2023-10-01&page=0&size=10")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_downloadLetterAttachment() throws IOException {
		setupCall()
			.withServicePath("/2281/letters/9bb97fd2-4410-4a4b-9019-fdd98f01bd7c/attachments/5a70a27f-997e-431e-9155-cc50d01e80c5")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of("application/pdf"))
			.withExpectedBinaryResponse("attachment.pdf")
			.sendRequestAndVerifyResponse();
	}
}
