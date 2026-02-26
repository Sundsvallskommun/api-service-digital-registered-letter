package se.sundsvall.digitalregisteredletter.api;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.api.model.LetterStatus;
import se.sundsvall.digitalregisteredletter.api.model.LetterStatusRequest;
import se.sundsvall.digitalregisteredletter.service.LetterService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureWebTestClient
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class StatusResourceTest {

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
	void getLetterStatuses_OK() {
		final var ids = List.of(
			"11111111-1111-1111-1111-111111111111",
			"22222222-2222-2222-2222-222222222222",
			"33333333-3333-3333-3333-333333333333");

		final var expected = List.of(
			new LetterStatus(ids.get(0), "PENDING", "COMPLETED"),
			new LetterStatus(ids.get(1), "NEW", "NOT_FOUND"),
			new LetterStatus(ids.get(2), "NOT_FOUND", "NOT_FOUND"));

		when(letterServiceMock.getLetterStatuses(MUNICIPALITY_ID, ids)).thenReturn(expected);

		final var response = webTestClient.post()
			.uri("/%s/status/letters".formatted(MUNICIPALITY_ID))
			.contentType(APPLICATION_JSON)
			.bodyValue(new LetterStatusRequest(ids))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(LetterStatus.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).usingRecursiveComparison().isEqualTo(expected);
		verify(letterServiceMock).getLetterStatuses(MUNICIPALITY_ID, ids);
	}
}
