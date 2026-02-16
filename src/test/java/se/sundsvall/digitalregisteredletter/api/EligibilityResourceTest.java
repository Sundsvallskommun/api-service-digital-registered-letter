package se.sundsvall.digitalregisteredletter.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.api.model.EligibilityRequest;
import se.sundsvall.digitalregisteredletter.service.EligibilityService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class EligibilityResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ORGANIZATION_NUMBER = "5591628136";

	@MockitoBean
	private EligibilityService eligibilityServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void noMoreInteractions() {
		verifyNoMoreInteractions(eligibilityServiceMock);
	}

	@Test
	void checkKivraEligibility() {
		// Parameter values
		final var partyId = "123e4567-e89b-12d3-a456-426614174000";
		final var request = new EligibilityRequest(List.of(partyId));
		final var eligiblePartyIds = List.of(partyId);

		// Mock
		when(eligibilityServiceMock.checkEligibility(MUNICIPALITY_ID, ORGANIZATION_NUMBER, request)).thenReturn(eligiblePartyIds);

		// Call
		final var response = webTestClient.post()
			.uri("/%s/%s/eligibility/kivra".formatted(MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectBody(new ParameterizedTypeReference<List<String>>() {

			})
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(response).containsExactly(partyId);
		verify(eligibilityServiceMock).checkEligibility(MUNICIPALITY_ID, ORGANIZATION_NUMBER, request);
	}

}
