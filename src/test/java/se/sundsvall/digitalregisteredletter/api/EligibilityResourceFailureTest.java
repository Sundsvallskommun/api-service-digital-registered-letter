package se.sundsvall.digitalregisteredletter.api;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.api.model.EligibilityRequest;
import se.sundsvall.digitalregisteredletter.service.EligibilityService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class EligibilityResourceFailureTest {

	private static final String BAD_MUNICIPALITY_ID = "bad-municipality-id";
	private static final String BAD_PARTY_ID = "not-a-valid-uuid";

	@MockitoBean
	private EligibilityService eligibilityServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void checkKivraEligibility_badMunicipalityId() {
		final var request = new EligibilityRequest(List.of("123e4567-e89b-12d3-a456-426614174000"));

		final var response = webTestClient.post()
			.uri("/%s/%s/eligibility/kivra".formatted(BAD_MUNICIPALITY_ID, "5591628136"))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("checkKivraEligibility.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(eligibilityServiceMock);
	}

	@Test
	void checkKivraEligibility_badPartyId() {
		final var request = new EligibilityRequest(List.of(BAD_PARTY_ID));

		final var response = webTestClient.post()
			.uri("/%s/%s/eligibility/kivra".formatted("2281", "5591628136"))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("partyIds[0]", "not a valid UUID"));

		verifyNoInteractions(eligibilityServiceMock);
	}

}
