package se.sundsvall.digitalregisteredletter.api;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.TestDataFactory.createLetterRequest;
import static se.sundsvall.TestDataFactory.createLetterResponse;
import static se.sundsvall.TestDataFactory.createLetterResponses;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.api.model.LetterResponse;
import se.sundsvall.digitalregisteredletter.api.model.LetterResponses;
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
		var letterResponses = createLetterResponses();
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
			.expectBody(LetterResponses.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).usingRecursiveComparison().isEqualTo(letterResponses);
		verify(letterServiceMock).getLetters(MUNICIPALITY_ID, pageable);
	}

	@Test
	void getLetter_OK() {
		var letterResponse = createLetterResponse();
		var letterId = "1234567890";

		when(letterServiceMock.getLetter(MUNICIPALITY_ID, letterId)).thenReturn(letterResponse);

		var response = webTestClient.get()
			.uri("/%s/letters/%s".formatted(MUNICIPALITY_ID, letterId))
			.exchange()
			.expectStatus().isOk()
			.expectBody(LetterResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).usingRecursiveComparison().isEqualTo(letterResponse);
		verify(letterServiceMock).getLetter(MUNICIPALITY_ID, letterId);
	}

	@Test
	void getLetter_NotFound() {
		var letterId = "1234567890";

		when(letterServiceMock.getLetter(MUNICIPALITY_ID, letterId)).thenThrow(Problem.valueOf(NOT_FOUND));

		webTestClient.get()
			.uri("/%s/letters/%s".formatted(MUNICIPALITY_ID, letterId))
			.exchange()
			.expectStatus().isNotFound();

		verify(letterServiceMock).getLetter(MUNICIPALITY_ID, letterId);
	}

	@Test
	void sendLetter_Created() {
		var letterId = "1234567890";
		var request = createLetterRequest();

		when(letterServiceMock.sendLetter(MUNICIPALITY_ID, request)).thenReturn(letterId);

		webTestClient.post()
			.uri("/%s/letters".formatted(MUNICIPALITY_ID))
			.bodyValue(request)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().valueEquals("Location", "/%s/letters/%s".formatted(MUNICIPALITY_ID, letterId));

		verify(letterServiceMock).sendLetter(MUNICIPALITY_ID, request);
	}

	private static MultiValueMap<String, String> createParameterMap(final Integer page, final Integer limit) {
		final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

		ofNullable(page).ifPresent(p -> parameters.add("page", p.toString()));
		ofNullable(limit).ifPresent(p -> parameters.add("limit", p.toString()));

		return parameters;
	}
}
