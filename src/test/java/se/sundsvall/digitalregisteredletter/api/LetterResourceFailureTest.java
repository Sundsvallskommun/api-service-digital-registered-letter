package se.sundsvall.digitalregisteredletter.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.TestDataFactory.createLetterRequest;

import java.io.OutputStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.api.model.Letter.Attachment;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequestBuilder;
import se.sundsvall.digitalregisteredletter.api.model.OrganizationBuilder;
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
	void noMoreInteractions() {
		verifyNoMoreInteractions(letterServiceMock);
	}

	@Test
	void getLetter_badMunicipalityId_badRequest() {
		final var letterId = "1234567890";

		final var response = webTestClient.get()
			.uri("/%s/letters/%s".formatted("bad-municipality-id", letterId))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getLetter.municipalityId", "not a valid municipality ID"));
	}

	@Test
	void getLetters_badMunicipalityId_badRequest() {
		final var response = webTestClient.get()
			.uri("/%s/letters".formatted("bad-municipality-id"))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getLetters.municipalityId", "not a valid municipality ID"));
	}

	@Test
	void getSigningInfo_badMunicipalityId_badRequest() {
		final var letterId = "1234567890";

		final var response = webTestClient.get()
			.uri("/%s/letters/%s/signinginfo".formatted("bad-municipality-id", letterId))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getSigningInformation.municipalityId", "not a valid municipality ID"));
	}

	@Test
	void sendLetter_badMunicipalityId_badRequest() {
		final var createLetterRequest = createLetterRequest();

		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("test1.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("test2.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letter", createLetterRequest);

		final var response = webTestClient.post()
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
			.containsExactly(tuple("sendLetter.municipalityId", "not a valid municipality ID"));
	}

	@Test
	void sendLetter_emptyRequestBody_badRequest() {
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("test1.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letter", LetterRequestBuilder.create().build());

		final var response = webTestClient.post()
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
			.containsExactlyInAnyOrder(
				tuple("body", "must not be blank"),
				tuple("contentType", "must be one of: [text/plain, text/html]"),
				tuple("organization", "must not be null"),
				tuple("partyId", "not a valid UUID"),
				tuple("subject", "must not be blank"),
				tuple("supportInfo", "must not be null"));
	}

	@Test
	void sendLetter_duplicateFileNames_badRequest() {
		final var createLetterRequest = createLetterRequest();
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("duplicate-name.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("duplicate-name.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letter", createLetterRequest);

		final var response = webTestClient.post()
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
			.containsExactly(tuple("files", "no duplicate file names allowed in the list of files"));
	}

	@Test
	void sendLetter_badContentType_badRequest() {
		final var createLetterRequest = createLetterRequest();
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("test1.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("test2.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("letter", createLetterRequest);

		final var response = webTestClient.post()
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
			.containsExactly(tuple("sendLetter.files", "content type must be application/pdf"));
	}

	@Test
	void sendLetter_emptyOrganization_badRequest() {
		final var createLetterRequest = createLetterRequest(OrganizationBuilder.create().withName(" ").build());
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("test1.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letter", createLetterRequest);

		final var response = webTestClient.post()
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
			.containsExactlyInAnyOrder(
				tuple("organization.name", "must not be blank"),
				tuple("organization.number", "must not be null"));
	}

	@Test
	void downloadLetterAttachment_notFound() {
		final var letterId = "11111111-1111-1111-1111-111111111111";
		final var attachmentId = "22222222-2222-2222-2222-222222222222";

		when(letterServiceMock.getLetterAttachment(MUNICIPALITY_ID, letterId, attachmentId))
			.thenThrow(Problem.valueOf(NOT_FOUND, "Entity not found"));

		final var response = webTestClient.get()
			.uri("/%s/letters/%s/attachments/%s".formatted(MUNICIPALITY_ID, letterId, attachmentId))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody(ThrowableProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).contains("Not Found");

		verify(letterServiceMock).getLetterAttachment(MUNICIPALITY_ID, letterId, attachmentId);
	}

	@Test
	void downloadLetterAttachment_streamingFailure() {
		final var letterId = "11111111-1111-1111-1111-111111111111";
		final var attachmentId = "22222222-2222-2222-2222-222222222222";

		when(letterServiceMock.getLetterAttachment(MUNICIPALITY_ID, letterId, attachmentId))
			.thenReturn(new Attachment(attachmentId, "file.pdf", APPLICATION_PDF.toString()));

		doThrow(Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to stream content for attachment with id '%s'".formatted(attachmentId)))
			.when(letterServiceMock)
			.writeAttachmentContent(eq(MUNICIPALITY_ID), eq(letterId), eq(attachmentId), any(OutputStream.class));

		final var response = webTestClient.get()
			.uri("/%s/letters/%s/attachments/%s".formatted(MUNICIPALITY_ID, letterId, attachmentId))
			.accept(APPLICATION_PDF)
			.exchange()
			.expectStatus().is5xxServerError()
			.expectBody(ThrowableProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Internal Server Error");
		assertThat(response.getDetail()).contains("Failed to stream content");

		verify(letterServiceMock).getLetterAttachment(MUNICIPALITY_ID, letterId, attachmentId);
		verify(letterServiceMock).writeAttachmentContent(eq(MUNICIPALITY_ID), eq(letterId), eq(attachmentId), any(OutputStream.class));
	}

	@ParameterizedTest
	@MethodSource("argumentsProvider")
	void downloadLetterAttachment_badRequest(final String letterId, final String attachmentId) {
		final var response = webTestClient.get()
			.uri("/%s/letters/%s/attachments/%s".formatted(MUNICIPALITY_ID, letterId, attachmentId))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
	}

	static Stream<Arguments> argumentsProvider() {
		return Stream.of(
			Arguments.of("11111111-1111-1111-1111-111111111111", "invalid-attachment-id"),
			Arguments.of("invalid-letter-id", "22222222-2222-2222-2222-222222222222"));
	}
}
