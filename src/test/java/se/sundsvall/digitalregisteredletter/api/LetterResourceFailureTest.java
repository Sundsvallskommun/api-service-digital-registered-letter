package se.sundsvall.digitalregisteredletter.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.TestDataFactory.createLetterRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.service.LetterService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class LetterResourceFailureTest {

	private static final String MUNICIPALITY_ID = "2281";

	@MockitoBean
	private LetterService letterServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void verifyNoMoreInteractions() {
		Mockito.verifyNoMoreInteractions(letterServiceMock);
	}

	@Test
	void getLetter_notFound() {
		var letterId = "1234567890";

		when(letterServiceMock.getLetter(MUNICIPALITY_ID, letterId)).thenThrow(Problem.valueOf(NOT_FOUND));

		webTestClient.get()
			.uri("/%s/letters/%s".formatted(MUNICIPALITY_ID, letterId))
			.exchange()
			.expectStatus().isNotFound();

		verify(letterServiceMock).getLetter(MUNICIPALITY_ID, letterId);
	}

	@Test
	void getLetter_badMunicipalityId_badRequest() {
		var letterId = "1234567890";

		var response = webTestClient.get()
			.uri("/%s/letters/%s".formatted("bad-municipality-id", letterId))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("getLetter.municipalityId", "not a valid municipality ID"));

		verify(letterServiceMock, never()).getLetter(any(), any());
	}

	@Test
	void getLetters_badMunicipalityId_badRequest() {
		var response = webTestClient.get()
			.uri("/%s/letters".formatted("bad-municipality-id"))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("getLetters.municipalityId", "not a valid municipality ID"));

		verify(letterServiceMock, never()).getLetters(any(), any());
	}

	@Test
	void sendLetter_badMunicipalityId_badRequest() {
		var createLetterRequest = createLetterRequest();

		var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("test1.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("test2.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letter", createLetterRequest);

		var response = webTestClient.post()
			.uri("/%s/letters".formatted("bad-municipality-id"))
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("sendLetter.municipalityId", "not a valid municipality ID"));

		verify(letterServiceMock, never()).parseLetterRequest(any());
		verify(letterServiceMock, never()).getLetters(any(), any());
	}

	@Test
	void sendLetter_duplicateFileNames_badRequest() {
		var createLetterRequest = createLetterRequest();
		var letterId = "1234567890";

		var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("duplicate-name.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("duplicate-name.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letter", createLetterRequest);

		when(letterServiceMock.parseLetterRequest(any())).thenReturn(createLetterRequest);
		when(letterServiceMock.sendLetter(any(), any(), any())).thenReturn(letterId);

		var response = webTestClient.post()
			.uri("/%s/letters".formatted(MUNICIPALITY_ID))
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("files", "no duplicate file names allowed in the list of files"));

		verify(letterServiceMock).parseLetterRequest(any());
		verify(letterServiceMock, never()).sendLetter(any(), any(), any());
	}

	@Test
	void sendLetter_badContentType_badRequest() {
		var createLetterRequest = createLetterRequest();
		var letterId = "1234567890";

		var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("test1.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("test2.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("letter", createLetterRequest);

		when(letterServiceMock.sendLetter(any(), any(), any())).thenReturn(letterId);

		var response = webTestClient.post()
			.uri("/%s/letters".formatted(MUNICIPALITY_ID))
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("sendLetter.files", "content type must be application/pdf"));

		verify(letterServiceMock, never()).parseLetterRequest(any());
		verify(letterServiceMock, never()).sendLetter(any(), any(), any());
	}

}
