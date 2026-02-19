package se.sundsvall.digitalregisteredletter.api;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.api.model.LetterStatusRequest;
import se.sundsvall.digitalregisteredletter.service.LetterService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class StatusResourceFailureTest {

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
	void getLetterStatuses_badMunicipalityId_badRequest() {
		final var letterIds = List.of(
			"11111111-1111-1111-1111-111111111111",
			"22222222-2222-2222-2222-222222222222");

		final var response = webTestClient.post()
			.uri("/%s/status/letters".formatted("bad-municipality-id"))
			.contentType(APPLICATION_JSON)
			.bodyValue(new LetterStatusRequest(letterIds))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getLetterStatuses.municipalityId", "not a valid municipality ID"));
	}

	@Test
	void getLetterStatuses_badLetterId_badRequest() {
		final var invalidLetterIds = List.of("invalid-uuid");

		final var response = webTestClient.post()
			.uri("/%s/status/letters".formatted(MUNICIPALITY_ID))
			.contentType(APPLICATION_JSON)
			.bodyValue(new LetterStatusRequest(invalidLetterIds))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("letterIds[0]", "not a valid UUID"));
	}

	@ParameterizedTest
	@NullAndEmptySource
	void getLetterStatuses_emptyLetterIdList_badRequest(List<String> letterIds) {
		final var response = webTestClient.post()
			.uri("/%s/status/letters".formatted(MUNICIPALITY_ID))
			.contentType(APPLICATION_JSON)
			.bodyValue(new LetterStatusRequest(letterIds))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("letterIds", "must not be empty"));
	}
}
