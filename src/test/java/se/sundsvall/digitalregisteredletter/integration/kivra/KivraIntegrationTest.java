package se.sundsvall.digitalregisteredletter.integration.kivra;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import se.sundsvall.dept44.exception.ServerProblem;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserV2;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.KeyValueBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponse;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponseBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.UserMatchV2SSN;
import se.sundsvall.digitalregisteredletter.service.TenantService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.NOT_IMPLEMENTED;
import static se.sundsvall.TestDataFactory.NOW;
import static se.sundsvall.digitalregisteredletter.Constants.STATUS_SENT;

@ExtendWith(MockitoExtension.class)
class KivraIntegrationTest {

	private static final String TENANT_KEY = "some-tenant-key";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String ORGANIZATION_NUMBER = "5591628136";

	@Mock
	private KivraMapper kivraMapperMock;

	@Mock
	private KivraClient kivraClientMock;

	@Mock
	private TenantService tenantServiceMock;

	@InjectMocks
	private KivraIntegration kivraIntegration;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(kivraMapperMock, kivraClientMock, tenantServiceMock);
	}

	@Test
	void checkEligibilityWithOrganizationNumber() {
		final var legalIds = List.of("1234567890");
		final var userMatchV2SSN = new UserMatchV2SSN(legalIds);

		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(userMatchV2SSN);
		when(kivraClientMock.checkEligibility(TENANT_KEY, userMatchV2SSN)).thenReturn(userMatchV2SSN);

		final var result = kivraIntegration.checkEligibility(legalIds, MUNICIPALITY_ID, ORGANIZATION_NUMBER);

		assertThat(result).isNotNull().hasSize(1).containsExactly("1234567890");

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraMapperMock).toCheckEligibilityRequest(legalIds);
		verify(kivraClientMock).checkEligibility(TENANT_KEY, userMatchV2SSN);
	}

	@Test
	void checkEligibilityWithOrganizationNumberKivraThrowsServerProblem() {
		final var legalIds = List.of("1234567890");
		final var request = new UserMatchV2SSN(legalIds);

		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(request);
		when(kivraClientMock.checkEligibility(TENANT_KEY, request)).thenThrow(new ServerProblem(NOT_IMPLEMENTED, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.checkEligibility(legalIds, MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Server exception occurred while checking Kivra eligibility for legal ids: %s", legalIds);

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraClientMock).checkEligibility(TENANT_KEY, request);
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
		when(kivraMapperMock.toSendContentRequest(letterEntity, legalId)).thenCallRealMethod();
		when(kivraClientMock.sendContent(eq(TENANT_KEY), any(ContentUserV2.class))).thenReturn(response);

		final var result = assertDoesNotThrow(() -> kivraIntegration.sendContent(letterEntity, legalId, MUNICIPALITY_ID, ORGANIZATION_NUMBER));

		assertThat(result).isEqualTo(STATUS_SENT);

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraMapperMock).toSendContentRequest(letterEntity, legalId);
		verify(kivraMapperMock).toRegisteredLetter(letterEntity.getId());
		verify(kivraMapperMock).toPartsResponsives(letterEntity.getAttachments());
		verify(kivraClientMock).sendContent(eq(TENANT_KEY), any(ContentUserV2.class));
	}

	@Test
	void getAllResponsesWithOrganizationNumber() {
		final var status = "signed";
		final var responseKey = "responseKey";
		final var keyValue = KeyValueBuilder.create()
			.withStatus(status)
			.withResponseKey(responseKey)
			.build();
		final var keyValues = List.of(keyValue);

		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		when(kivraClientMock.getAllResponses(TENANT_KEY)).thenReturn(keyValues);

		final var result = kivraIntegration.getAllResponses(MUNICIPALITY_ID, ORGANIZATION_NUMBER);

		assertThat(result).isNotNull().hasSize(1).allSatisfy(pair -> {
			assertThat(pair.status()).isEqualTo(status);
			assertThat(pair.responseKey()).isEqualTo(responseKey);
		});

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraClientMock).getAllResponses(TENANT_KEY);
	}

	@Test
	void getAllResponsesWithOrganizationNumberWhenKivraRespondsWithNull() {
		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		when(kivraClientMock.getAllResponses(TENANT_KEY)).thenReturn(null);

		assertThat(kivraIntegration.getAllResponses(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).isNotNull().isEmpty();

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraClientMock).getAllResponses(TENANT_KEY);
	}

	@Test
	void getAllResponsesWithOrganizationNumberKivraThrowsServerProblem() {
		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		when(kivraClientMock.getAllResponses(TENANT_KEY)).thenThrow(new ServerProblem(NOT_IMPLEMENTED, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.getAllResponses(MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Server exception occurred while retrieving Kivra responses");

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraClientMock).getAllResponses(TENANT_KEY);
	}

	@Test
	void getAllResponsesWithOrganizationNumberThrowsException() {
		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		when(kivraClientMock.getAllResponses(TENANT_KEY)).thenThrow(new RuntimeException("Fasten your seatbelts"));

		assertThatThrownBy(() -> kivraIntegration.getAllResponses(MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while retrieving Kivra responses");

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraClientMock).getAllResponses(TENANT_KEY);
	}

	@Test
	void getRegisteredLetterResponseWithOrganizationNumber() {
		final var responseKey = "responseKey";
		final var registeredLetterResponse = RegisteredLetterResponseBuilder.create()
			.withStatus("signed")
			.withSignedAt(NOW)
			.withSenderReference(new RegisteredLetterResponse.SenderReference("internalId"))
			.build();

		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		when(kivraClientMock.getResponseDetails(TENANT_KEY, responseKey)).thenReturn(registeredLetterResponse);

		final var result = kivraIntegration.getRegisteredLetterResponse(responseKey, MUNICIPALITY_ID, ORGANIZATION_NUMBER);

		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo("signed");
		assertThat(result.signedAt()).isEqualTo(NOW);
		assertThat(result.senderReference().internalId()).isEqualTo("internalId");

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraClientMock).getResponseDetails(TENANT_KEY, responseKey);
	}

	@Test
	void getRegisteredLetterResponseWithOrganizationNumberKivraThrowsServerProblem() {
		final var responseKey = "responseKey";

		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		when(kivraClientMock.getResponseDetails(TENANT_KEY, responseKey)).thenThrow(new ServerProblem(NOT_IMPLEMENTED, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.getRegisteredLetterResponse(responseKey, MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Exception occurred while retrieving Kivra registered letter response for responseKey: %s", responseKey);

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraClientMock).getResponseDetails(TENANT_KEY, responseKey);
	}

	@Test
	void getRegisteredLetterResponseWithOrganizationNumberThrowsException() {
		final var responseKey = "responseKey";

		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		when(kivraClientMock.getResponseDetails(TENANT_KEY, responseKey)).thenThrow(new RuntimeException("Fasten your seatbelts"));

		assertThatThrownBy(() -> kivraIntegration.getRegisteredLetterResponse(responseKey, MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while retrieving Kivra registered letter response for responseKey: %s", responseKey);

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraClientMock).getResponseDetails(TENANT_KEY, responseKey);
	}

	@Test
	void deleteResponseWithOrganizationNumber() {
		final var responseKey = "responseKey";

		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);

		kivraIntegration.deleteResponse(responseKey, MUNICIPALITY_ID, ORGANIZATION_NUMBER);

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraClientMock).deleteResponse(TENANT_KEY, responseKey);
	}

	@Test
	void deleteResponseWithOrganizationNumberKivraThrowsServerProblem() {
		final var responseKey = "responseKey";

		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		doThrow(new ServerProblem(NOT_IMPLEMENTED, "Damn you Salazar")).when(kivraClientMock).deleteResponse(TENANT_KEY, responseKey);

		assertThatThrownBy(() -> kivraIntegration.deleteResponse(responseKey, MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Server exception occurred while deleting Kivra response for responseKey: %s", responseKey);

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraClientMock).deleteResponse(TENANT_KEY, responseKey);
	}

	@Test
	void deleteResponseWithOrganizationNumberThrowsException() {
		final var responseKey = "responseKey";

		when(tenantServiceMock.getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(TENANT_KEY);
		doThrow(new RuntimeException("Fasten your seatbelts")).when(kivraClientMock).deleteResponse(TENANT_KEY, responseKey);

		assertThatThrownBy(() -> kivraIntegration.deleteResponse(responseKey, MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while deleting Kivra response for responseKey: %s", responseKey);

		verify(tenantServiceMock).getDecryptedTenantKey(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(kivraClientMock).deleteResponse(TENANT_KEY, responseKey);
	}

	@Test
	void healthCheck() {
		kivraIntegration.healthCheck();

		verify(kivraClientMock).getTenantInformation();
	}
}
