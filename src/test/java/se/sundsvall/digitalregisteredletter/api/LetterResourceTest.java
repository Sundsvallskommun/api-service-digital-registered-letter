package se.sundsvall.digitalregisteredletter.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.TestDataFactory.createLetter;
import static se.sundsvall.TestDataFactory.createLetterRequest;
import static se.sundsvall.TestDataFactory.createLetters;
import static se.sundsvall.TestDataFactory.createSigningInfo;

import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilterBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfo;
import se.sundsvall.digitalregisteredletter.service.LetterService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class LetterResourceTest {

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
	void getLetters_OK() {
		final var letterResponses = createLetters();
		final var pageNumber = 0;
		final var pageSize = 10;
		final var pageable = PageRequest.of(pageNumber, pageSize);
		final var letterFilter = LetterFilterBuilder.create().build();

		when(letterServiceMock.getLetters(eq(MUNICIPALITY_ID), eq(letterFilter), any(Pageable.class))).thenReturn(letterResponses);

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/%s/letters".formatted(MUNICIPALITY_ID))
				.queryParam("page", pageNumber)
				.queryParam("size", pageSize)
				.build())
			.exchange()
			.expectStatus().isOk()
			.expectBody(Letters.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).usingRecursiveComparison().isEqualTo(letterResponses);
		verify(letterServiceMock).getLetters(MUNICIPALITY_ID, letterFilter, pageable);
	}

	@Test
	void getLetter_OK() {
		final var letterResponse = createLetter();
		final var letterId = UUID.randomUUID().toString();

		when(letterServiceMock.getLetter(MUNICIPALITY_ID, letterId)).thenReturn(letterResponse);

		final var response = webTestClient.get()
			.uri("/%s/letters/%s".formatted(MUNICIPALITY_ID, letterId))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Letter.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).usingRecursiveComparison().isEqualTo(letterResponse);
		verify(letterServiceMock).getLetter(MUNICIPALITY_ID, letterId);
	}

	@Test
	void getLetter_notFound() {
		final var letterId = UUID.randomUUID().toString();

		when(letterServiceMock.getLetter(MUNICIPALITY_ID, letterId)).thenThrow(Problem.valueOf(NOT_FOUND));

		webTestClient.get()
			.uri("/%s/letters/%s".formatted(MUNICIPALITY_ID, letterId))
			.exchange()
			.expectStatus().isNotFound();

		verify(letterServiceMock).getLetter(MUNICIPALITY_ID, letterId);
	}

	@Test
	void getSigningInfo_OK() {
		final var signingInfoResponse = createSigningInfo();
		final var letterId = UUID.randomUUID().toString();

		when(letterServiceMock.getSigningInformation(MUNICIPALITY_ID, letterId)).thenReturn(signingInfoResponse);

		final var response = webTestClient.get()
			.uri("/%s/letters/%s/signinginfo".formatted(MUNICIPALITY_ID, letterId))
			.exchange()
			.expectStatus().isOk()
			.expectBody(SigningInfo.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).usingRecursiveComparison().isEqualTo(signingInfoResponse);
		verify(letterServiceMock).getSigningInformation(MUNICIPALITY_ID, letterId);
	}

	@Test
	void sendLetter_Created() {
		final var organizationNumber = "5591628136";
		final var createLetterRequest = createLetterRequest();
		final var letterId = UUID.randomUUID().toString();
		final var letterResponse = createLetter(letterId);

		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("test1.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("tesst2.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letter", createLetterRequest);

		when(letterServiceMock.sendLetter(any(), any(), any(), any())).thenReturn(letterResponse);

		final var response = webTestClient.post()
			.uri("/%s/%s/letters".formatted(MUNICIPALITY_ID, organizationNumber))
			.contentType(MULTIPART_FORM_DATA)
			.header(Identifier.HEADER_NAME, "type=adAccount; test01user")
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().valueEquals("Location", "/%s/%s/letters/%s".formatted(MUNICIPALITY_ID, organizationNumber, letterId))
			.expectBody(Letter.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).usingRecursiveComparison().isEqualTo(letterResponse);
		verify(letterServiceMock).sendLetter(any(), any(), any(), any());
	}

	@Test
	void sendLetterLegacy_Created() {
		final var createLetterRequest = createLetterRequest();
		final var letterId = UUID.randomUUID().toString();
		final var letterResponse = createLetter(letterId);

		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("test1.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("tesst2.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letter", createLetterRequest);

		when(letterServiceMock.sendLetter(any(), any(), any())).thenReturn(letterResponse);

		final var response = webTestClient.post()
			.uri("/%s/letters".formatted(MUNICIPALITY_ID))
			.contentType(MULTIPART_FORM_DATA)
			.header(Identifier.HEADER_NAME, "type=adAccount; test01user")
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().valueEquals("Location", "/%s/letters/%s".formatted(MUNICIPALITY_ID, letterId))
			.expectBody(Letter.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).usingRecursiveComparison().isEqualTo(letterResponse);
		verify(letterServiceMock).sendLetter(any(), any(), any());
	}

	@Test
	void downloadLetterAttachment_OK() {
		final var letterId = "11111111-1111-1111-1111-111111111111";
		final var attachmentId = "22222222-2222-2222-2222-222222222222";
		final var bytes = "some-random-content".getBytes();

		doAnswer(invocation -> {
			final var response = (HttpServletResponse) invocation.getArgument(3);
			response.addHeader("Content-Type", "application/pdf");
			response.addHeader("Content-Disposition", "attachment; filename=\"file.pdf\"");
			response.getOutputStream().write(bytes);
			return null;
		}).when(letterServiceMock)
			.readLetterAttachment(eq(MUNICIPALITY_ID), eq(letterId), eq(attachmentId), any(HttpServletResponse.class));

		final var responseBytes = webTestClient.get()
			.uri("/%s/letters/%s/attachments/%s".formatted(MUNICIPALITY_ID, letterId, attachmentId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_PDF)
			.expectHeader().valueMatches("Content-Disposition", ".*attachment;.*file\\.pdf.*")
			.expectBody(byte[].class)
			.returnResult()
			.getResponseBody();

		assertThat(responseBytes).isNotNull().isEqualTo(bytes);

		verify(letterServiceMock).readLetterAttachment(eq(MUNICIPALITY_ID), eq(letterId), eq(attachmentId), any(HttpServletResponse.class));
	}

	@Test
	void downloadLetterAttachment_fallbackContentType() {
		final var letterId = "11111111-1111-1111-1111-111111111111";
		final var attachmentId = "22222222-2222-2222-2222-222222222222";

		doAnswer(invocation -> {
			final var response = (HttpServletResponse) invocation.getArgument(3);
			response.addHeader("Content-Type", "application/octet-stream");
			response.addHeader("Content-Disposition", "attachment; filename=\"some.file\"");
			return null;
		}).when(letterServiceMock)
			.readLetterAttachment(eq(MUNICIPALITY_ID), eq(letterId), eq(attachmentId), any(HttpServletResponse.class));

		webTestClient.get()
			.uri("/%s/letters/%s/attachments/%s".formatted(MUNICIPALITY_ID, letterId, attachmentId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_OCTET_STREAM);

		verify(letterServiceMock).readLetterAttachment(eq(MUNICIPALITY_ID), eq(letterId), eq(attachmentId), any(HttpServletResponse.class));
	}

	@Test
	void readLetterReceipt() {
		final var letterId = "11111111-1111-1111-1111-111111111111";

		doAnswer(invocation -> {
			final var response = (HttpServletResponse) invocation.getArgument(2);
			response.addHeader("Content-Type", "application/pdf");
			response.addHeader("Content-Disposition", "attachment; filename=\"kvittens_rekutskick_" + letterId + ".pdf\"");
			response.getOutputStream().write("some-random-content".getBytes());
			return null;
		}).when(letterServiceMock)
			.readLetterReceipt(eq(MUNICIPALITY_ID), eq(letterId), any(HttpServletResponse.class));

		webTestClient.get()
			.uri("/%s/letters/%s/receipt".formatted(MUNICIPALITY_ID, letterId))
			.exchange()
			.expectStatus().isOk();

		verify(letterServiceMock).readLetterReceipt(eq(MUNICIPALITY_ID), eq(letterId), any(HttpServletResponse.class));

	}
}
