package se.sundsvall.digitalregisteredletter.integration.kivra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_IMPLEMENTED;
import static org.zalando.problem.Status.SEE_OTHER;
import static se.sundsvall.TestDataFactory.NOW;
import static se.sundsvall.digitalregisteredletter.Constants.STATUS_SENT;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.exception.ServerProblem;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.configuration.KivraProperties;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserV2;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.KeyValueBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponse;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponseBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.UserMatchV2SSN;
import se.sundsvall.digitalregisteredletter.service.TenantService;

@ExtendWith(MockitoExtension.class)
class KivraIntegrationTest {

	private static final String API_URL = "http://kivra-url.com/some-tenant-key";
	private static final String BASE_URL = "http://kivra-url.com";
	private static final String TENANT_KEY = "some-tenant-key";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String ORGANIZATION_NUMBER = "5591628136";
	private static final URI LEGACY_URI = URI.create(API_URL);
	private static final URI TENANT_URI = URI.create(BASE_URL + "/" + TENANT_KEY);

	@Mock
	private KivraMapper kivraMapperMock;

	@Mock
	private KivraClient kivraClientMock;

	@Mock
	private KivraProperties kivraPropertiesMock;

	@Mock
	private TenantService tenantServiceMock;

	@InjectMocks
	private KivraIntegration kivraIntegration;

	private static Stream<Arguments> provideResponseProblems() {
		return Stream.of(
			Arguments.of(new ClientProblem(BAD_REQUEST, "Damn you Salazar"), "FAILED - Client Error"),
			Arguments.of(new ServerProblem(NOT_IMPLEMENTED, "You fool of a Took"), "FAILED - Server Error"),
			Arguments.arguments(Problem.valueOf(SEE_OTHER, "Go ahead, make my day"), "FAILED - Unknown Error"));
	}

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(kivraMapperMock, kivraClientMock, kivraPropertiesMock, tenantServiceMock);
	}

	@Test
	void checkEligibilityWithOrganizationNumber() {
		final var legalIds = List.of("1234567890");
		final var userMatchV2SSN = new UserMatchV2SSN(legalIds);

		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		when(kivraPropertiesMock.baseUrl()).thenReturn(BASE_URL);
		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(userMatchV2SSN);
		when(kivraClientMock.checkEligibility(TENANT_URI, userMatchV2SSN)).thenReturn(userMatchV2SSN);

		final var result = kivraIntegration.checkEligibility(legalIds, MUNICIPALITY_ID, ORGANIZATION_NUMBER);

		assertThat(result).isNotNull().hasSize(1).containsExactly("1234567890");

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraPropertiesMock).baseUrl();
		verify(kivraMapperMock).toCheckEligibilityRequest(legalIds);
		verify(kivraClientMock).checkEligibility(TENANT_URI, userMatchV2SSN);
	}

	@Test
	void checkEligibilityWithOrganizationNumberKivraThrowsServerProblem() {
		final var legalIds = List.of("1234567890");
		final var request = new UserMatchV2SSN(legalIds);

		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		when(kivraPropertiesMock.baseUrl()).thenReturn(BASE_URL);
		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(request);
		when(kivraClientMock.checkEligibility(TENANT_URI, request)).thenThrow(new ServerProblem(NOT_IMPLEMENTED, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.checkEligibility(legalIds, MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Server exception occurred while checking Kivra eligibility for legal ids: %s", legalIds);

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraPropertiesMock).baseUrl();
		verify(kivraClientMock).checkEligibility(TENANT_URI, request);
	}

	@Test
	void checkEligibilityLegacy() {
		final var legalIds = List.of("1234567890");
		final var userMatchV2SSN = new UserMatchV2SSN(legalIds);

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(userMatchV2SSN);
		when(kivraClientMock.checkEligibility(LEGACY_URI, userMatchV2SSN)).thenReturn(userMatchV2SSN);

		final var result = kivraIntegration.checkEligibility(legalIds);

		assertThat(result).isNotNull().hasSize(1).containsExactly("1234567890");

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraMapperMock).toCheckEligibilityRequest(legalIds);
		verify(kivraClientMock).checkEligibility(LEGACY_URI, userMatchV2SSN);
	}

	@Test
	void checkEligibilityLegacyKivraThrowsServerProblem() {
		final var legalIds = List.of("1234567890");
		final var request = new UserMatchV2SSN(legalIds);

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(request);
		when(kivraClientMock.checkEligibility(LEGACY_URI, request)).thenThrow(new ServerProblem(NOT_IMPLEMENTED, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.checkEligibility(legalIds))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Server exception occurred while checking Kivra eligibility for legal ids: %s", legalIds);

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).checkEligibility(LEGACY_URI, request);
	}

	@Test
	void checkEligibilityLegacyKivraThrowsClientProblem() {
		final var legalIds = List.of("1234567890");
		final var request = new UserMatchV2SSN(legalIds);

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(request);
		when(kivraClientMock.checkEligibility(LEGACY_URI, request)).thenThrow(new ClientProblem(BAD_REQUEST, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.checkEligibility(legalIds))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while checking Kivra eligibility for legal ids: %s", legalIds);

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).checkEligibility(LEGACY_URI, request);
	}

	@Test
	void checkEligibilityLegacyThrowsException() {
		final var legalIds = List.of("1234567890");
		final var request = new UserMatchV2SSN(legalIds);

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(request);
		when(kivraClientMock.checkEligibility(LEGACY_URI, request)).thenThrow(new RuntimeException("Fasten your seatbelts"));

		assertThatThrownBy(() -> kivraIntegration.checkEligibility(legalIds))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while checking Kivra eligibility for legal ids: %s", legalIds);

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).checkEligibility(LEGACY_URI, request);
	}

	@Test
	void sendContentWithOrganizationNumber() {
		final var letterEntity = new LetterEntity();
		final var legalId = "1234567890";
		final var response = ContentUserBuilder.create()
			.withSubject("subject")
			.withGeneratedAt(LocalDate.MIN)
			.withType("registered.letter")
			.build();

		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		when(kivraPropertiesMock.baseUrl()).thenReturn(BASE_URL);
		when(kivraMapperMock.toSendContentRequest(letterEntity, legalId)).thenCallRealMethod();
		when(kivraClientMock.sendContent(eq(TENANT_URI), any(ContentUserV2.class))).thenReturn(response);

		final var result = assertDoesNotThrow(() -> kivraIntegration.sendContent(letterEntity, legalId, MUNICIPALITY_ID, ORGANIZATION_NUMBER));

		assertThat(result).isEqualTo(STATUS_SENT);

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraPropertiesMock).baseUrl();
		verify(kivraMapperMock).toSendContentRequest(letterEntity, legalId);
		verify(kivraMapperMock).toRegisteredLetter(letterEntity.getId());
		verify(kivraMapperMock).toPartsResponsives(letterEntity.getAttachments());
		verify(kivraClientMock).sendContent(eq(TENANT_URI), any(ContentUserV2.class));
	}

	@Test
	void sendContentLegacy() {
		final var letterEntity = new LetterEntity();
		final var legalId = "1234567890";
		final var response = ContentUserBuilder.create()
			.withSubject("subject")
			.withGeneratedAt(LocalDate.MIN)
			.withType("registered.letter")
			.build();

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraMapperMock.toSendContentRequest(letterEntity, legalId)).thenCallRealMethod();
		when(kivraClientMock.sendContent(eq(LEGACY_URI), any(ContentUserV2.class))).thenReturn(response);

		final var result = assertDoesNotThrow(() -> kivraIntegration.sendContent(letterEntity, legalId));

		assertThat(result).isEqualTo(STATUS_SENT);

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraMapperMock).toSendContentRequest(letterEntity, legalId);
		verify(kivraMapperMock).toRegisteredLetter(letterEntity.getId());
		verify(kivraMapperMock).toPartsResponsives(letterEntity.getAttachments());
		verify(kivraClientMock).sendContent(eq(LEGACY_URI), any(ContentUserV2.class));
	}

	@ParameterizedTest
	@MethodSource("provideResponseProblems")
	void sendContentLegacyKivraThrowsProblem(final ThrowableProblem problem, final String expectedResult) {
		final var letterEntity = new LetterEntity();
		final var legalId = "1234567890";

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraClientMock.sendContent(any(URI.class), any())).thenThrow(problem);

		final var result = assertDoesNotThrow(() -> kivraIntegration.sendContent(letterEntity, legalId));

		assertThat(result).isEqualTo(expectedResult);
		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).sendContent(any(URI.class), any());
		verify(kivraMapperMock).toSendContentRequest(letterEntity, legalId);
	}

	@Test
	void sendContentLegacyThrowsException() {
		final var letterEntity = new LetterEntity();
		final var legalId = "1234567890";

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraClientMock.sendContent(any(URI.class), any())).thenThrow(new RuntimeException("Testexception"));

		assertThatThrownBy(() -> kivraIntegration.sendContent(letterEntity, legalId))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while sending content to Kivra for legal id: %s", legalId);

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).sendContent(any(URI.class), any());
		verify(kivraMapperMock).toSendContentRequest(letterEntity, legalId);
	}

	@Test
	void getAllResponses() {
		final var status = "signed";
		final var responseKey = "responseKey";
		final var keyValue = KeyValueBuilder.create()
			.withStatus(status)
			.withResponseKey(responseKey)
			.build();
		final var keyValues = List.of(keyValue);

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraClientMock.getAllResponses(LEGACY_URI)).thenReturn(keyValues);

		final var result = kivraIntegration.getAllResponses();

		assertThat(result).isNotNull().hasSize(1).allSatisfy(pair -> {
			assertThat(pair.status()).isEqualTo(status);
			assertThat(pair.responseKey()).isEqualTo(responseKey);
		});

		verify(kivraPropertiesMock).apiUrl();
	}

	@Test
	void getAllResponsesWhenKivraRespondsWithNull() {
		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraClientMock.getAllResponses(LEGACY_URI)).thenReturn(null);

		assertThat(kivraIntegration.getAllResponses()).isNotNull().isEmpty();

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).getAllResponses(LEGACY_URI);
	}

	@Test
	void getAllResponsesKivraThrowsServerProblem() {
		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraClientMock.getAllResponses(LEGACY_URI)).thenThrow(new ServerProblem(NOT_IMPLEMENTED, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.getAllResponses())
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Server exception occurred while retrieving Kivra responses");

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).getAllResponses(LEGACY_URI);
	}

	@Test
	void getAllResponsesKivraThrowsClientProblem() {
		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraClientMock.getAllResponses(LEGACY_URI)).thenThrow(new ClientProblem(BAD_REQUEST, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.getAllResponses())
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while retrieving Kivra responses");

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).getAllResponses(LEGACY_URI);
	}

	@Test
	void getAllResponsesThrowsException() {
		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraClientMock.getAllResponses(LEGACY_URI)).thenThrow(new RuntimeException("Fasten your seatbelts"));

		assertThatThrownBy(() -> kivraIntegration.getAllResponses())
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while retrieving Kivra responses");

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).getAllResponses(LEGACY_URI);
	}

	@Test
	void getRegisteredLetterResponse() {
		final var responseKey = "responseKey";
		final var registeredLetterResponse = RegisteredLetterResponseBuilder.create()
			.withStatus("signed")
			.withSignedAt(NOW)
			.withSenderReference(new RegisteredLetterResponse.SenderReference("internalId"))
			.build();

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraClientMock.getResponseDetails(LEGACY_URI, responseKey)).thenReturn(registeredLetterResponse);

		final var result = kivraIntegration.getRegisteredLetterResponse(responseKey);

		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo("signed");
		assertThat(result.signedAt()).isEqualTo(NOW);
		assertThat(result.senderReference().internalId()).isEqualTo("internalId");

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).getResponseDetails(LEGACY_URI, responseKey);
	}

	@Test
	void getRegisteredLetterResponseKivraThrowsServerProblem() {
		final var responseKey = "responseKey";

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraClientMock.getResponseDetails(LEGACY_URI, responseKey)).thenThrow(new ServerProblem(NOT_IMPLEMENTED, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.getRegisteredLetterResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Exception occurred while retrieving Kivra registered letter response for responseKey: %s", responseKey);

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).getResponseDetails(LEGACY_URI, responseKey);
	}

	@Test
	void getRegisteredLetterResponseKivraThrowsClientProblem() {
		final var responseKey = "responseKey";

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraClientMock.getResponseDetails(LEGACY_URI, responseKey)).thenThrow(new ClientProblem(BAD_REQUEST, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.getRegisteredLetterResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while retrieving Kivra registered letter response for responseKey: %s", responseKey);

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).getResponseDetails(LEGACY_URI, responseKey);
	}

	@Test
	void getRegisteredLetterResponseThrowsException() {
		final var responseKey = "responseKey";

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		when(kivraClientMock.getResponseDetails(LEGACY_URI, responseKey)).thenThrow(new RuntimeException("Fasten your seatbelts"));

		assertThatThrownBy(() -> kivraIntegration.getRegisteredLetterResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while retrieving Kivra registered letter response for responseKey: %s", responseKey);

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).getResponseDetails(LEGACY_URI, responseKey);
	}

	@Test
	void deleteResponse() {
		final var responseKey = "responseKey";

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);

		kivraIntegration.deleteResponse(responseKey);

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).deleteResponse(LEGACY_URI, responseKey);
	}

	@Test
	void deleteResponseKivraThrowsServerProblem() {
		final var responseKey = "responseKey";

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		doThrow(new ServerProblem(NOT_IMPLEMENTED, "Damn you Salazar")).when(kivraClientMock).deleteResponse(LEGACY_URI, responseKey);

		assertThatThrownBy(() -> kivraIntegration.deleteResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Server exception occurred while deleting Kivra response for responseKey: %s", responseKey);

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).deleteResponse(LEGACY_URI, responseKey);
	}

	@Test
	void deleteResponseKivraThrowsClientProblem() {
		final var responseKey = "responseKey";

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		doThrow(new ClientProblem(BAD_REQUEST, "Damn you Salazar")).when(kivraClientMock).deleteResponse(LEGACY_URI, responseKey);

		assertThatThrownBy(() -> kivraIntegration.deleteResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while deleting Kivra response for responseKey: %s", responseKey);

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).deleteResponse(LEGACY_URI, responseKey);
	}

	@Test
	void deleteResponseThrowsException() {
		final var responseKey = "responseKey";

		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);
		doThrow(new RuntimeException("Fasten your seatbelts")).when(kivraClientMock).deleteResponse(LEGACY_URI, responseKey);

		assertThatThrownBy(() -> kivraIntegration.deleteResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while deleting Kivra response for responseKey: %s", responseKey);

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).deleteResponse(LEGACY_URI, responseKey);
	}

	@Test
	void healthCheck() {
		when(kivraPropertiesMock.apiUrl()).thenReturn(API_URL);

		kivraIntegration.healthCheck();

		verify(kivraPropertiesMock).apiUrl();
		verify(kivraClientMock).getTenantInformation(LEGACY_URI);
	}
}
