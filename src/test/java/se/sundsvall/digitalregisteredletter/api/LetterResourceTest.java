package se.sundsvall.digitalregisteredletter.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;
import static se.sundsvall.TestDataFactory.createLetter;
import static se.sundsvall.TestDataFactory.createLetterRequest;
import static se.sundsvall.TestDataFactory.createLetters;

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
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
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
		var letterResponses = createLetters();
		var pageNumber = 0;
		var pageSize = 10;
		var pageable = PageRequest.of(pageNumber, pageSize);

		when(letterServiceMock.getLetters(eq(MUNICIPALITY_ID), any(Pageable.class))).thenReturn(letterResponses);

		var response = webTestClient.get()
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
		verify(letterServiceMock).getLetters(MUNICIPALITY_ID, pageable);
	}

	@Test
	void getLetter_OK() {
		var letterResponse = createLetter();
		var letterId = "1234567890";

		when(letterServiceMock.getLetter(MUNICIPALITY_ID, letterId)).thenReturn(letterResponse);

		var response = webTestClient.get()
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
	void sendLetter_Created() {
		var createLetterRequest = createLetterRequest();
		var letterId = "1234567890";

		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("test1.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letterAttachments", "file-content").filename("tesst2.txt").contentType(APPLICATION_PDF);
		multipartBodyBuilder.part("letter", createLetterRequest);

		when(letterServiceMock.sendLetter(any(), any(), any())).thenReturn(letterId);

		webTestClient.post()
			.uri("/%s/letters".formatted(MUNICIPALITY_ID))
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().valueEquals("Location", "/%s/letters/%s".formatted(MUNICIPALITY_ID, letterId));

		verify(letterServiceMock).sendLetter(any(), any(), any());
	}
}
