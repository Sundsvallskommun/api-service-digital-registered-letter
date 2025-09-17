package se.sundsvall.digitalregisteredletter.integration.kivra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_IMPLEMENTED;
import static org.zalando.problem.Status.SEE_OTHER;
import static se.sundsvall.TestDataFactory.NOW;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.exception.ServerProblem;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserV2;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.KeyValueBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponse;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponseBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.UserMatchV2SSN;

@ExtendWith(MockitoExtension.class)
class KivraIntegrationTest {

	@Mock
	private KivraMapper kivraMapperMock;

	@Mock
	private KivraClient kivraClientMock;

	@InjectMocks
	private KivraIntegration kivraIntegration;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(kivraMapperMock, kivraClientMock);
	}

	@Test
	void checkEligibilityTest() {
		final var legalIds = List.of("1234567890");
		final var userMatchV2SSN = new UserMatchV2SSN(legalIds);
		final var requestCaptor = ArgumentCaptor.forClass(UserMatchV2SSN.class);

		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(userMatchV2SSN);
		when(kivraClientMock.checkEligibility(requestCaptor.capture())).thenReturn(userMatchV2SSN);

		final var result = kivraIntegration.checkEligibility(legalIds);
		assertThat(result).isNotNull().hasSize(1).containsExactly(legalIds.getFirst());

		final var capturedRequest = requestCaptor.getValue();
		assertThat(capturedRequest).isNotNull();
		assertThat(capturedRequest.legalIds()).hasSize(1).containsExactly(legalIds.getFirst());

		verify(kivraMapperMock).toCheckEligibilityRequest(legalIds);
		verify(kivraClientMock).checkEligibility(capturedRequest);
	}

	@Test
	void checkEligibilityKivraThrowsServerProblem() {
		final var legalId = "1234567890";
		final var legalIds = List.of(legalId);
		final var request = new UserMatchV2SSN(legalIds);
		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(request);
		when(kivraClientMock.checkEligibility(request)).thenThrow(new ServerProblem(NOT_IMPLEMENTED, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.checkEligibility(legalIds))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Server exception occurred while checking Kivra eligibility for legal ids: %s", legalIds);

		verify(kivraClientMock).checkEligibility(request);
	}

	@Test
	void checkEligibilityKivraThrowsClientProblem() {
		final var legalId = "1234567890";
		final var legalIds = List.of(legalId);
		final var request = new UserMatchV2SSN(legalIds);
		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(request);
		when(kivraClientMock.checkEligibility(request)).thenThrow(new ClientProblem(BAD_REQUEST, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.checkEligibility(legalIds))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while checking Kivra eligibility for legal ids: %s", legalIds);

		verify(kivraClientMock).checkEligibility(request);
	}

	@Test
	void checkEligibilityThrowsException() {
		final var legalId = "1234567890";
		final var legalIds = List.of(legalId);
		final var request = new UserMatchV2SSN(legalIds);
		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(request);
		when(kivraClientMock.checkEligibility(request)).thenThrow(new RuntimeException("Fasten your seatbelts, it's going to be a bumpy ride"));

		assertThatThrownBy(() -> kivraIntegration.checkEligibility(legalIds))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while checking Kivra eligibility for legal ids: %s", legalIds);

		verify(kivraClientMock).checkEligibility(request);
	}

	@Test
	void sendContent() {
		final var letterEntity = new LetterEntity();
		final var legalId = "1234567890";
		final var response = ContentUserBuilder.create()
			.withSubject("subject")
			.withGeneratedAt(LocalDate.MIN)
			.withType("registered.letter")
			.build();

		final var requestCaptor = ArgumentCaptor.forClass(ContentUserV2.class);
		when(kivraMapperMock.toSendContentRequest(letterEntity, legalId)).thenCallRealMethod();
		when(kivraClientMock.sendContent(requestCaptor.capture())).thenReturn(response);

		final var result = assertDoesNotThrow(() -> kivraIntegration.sendContent(letterEntity, legalId));

		assertThat(result).isEqualTo("SENT");

		final var capturedRequest = requestCaptor.getValue();
		assertThat(capturedRequest).isNotNull().isInstanceOf(ContentUserV2.class);
		assertThat(capturedRequest.subject()).isEqualTo(letterEntity.getSubject());
		assertThat(capturedRequest.legalId()).isEqualTo(legalId);
		assertThat(capturedRequest.type()).isEqualTo("registered.letter");

		verify(kivraMapperMock).toSendContentRequest(letterEntity, legalId);
		verify(kivraMapperMock).toRegisteredLetter(letterEntity.getId());
		verify(kivraMapperMock).toPartsResponsives(letterEntity.getAttachments());
		verify(kivraClientMock).sendContent(capturedRequest);
	}

	private static Stream<Arguments> provideResponseProblems() {
		return Stream.of(
			Arguments.of(new ClientProblem(BAD_REQUEST, "Damn you Salazar"), "FAILED - Client Error"),
			Arguments.of(new ServerProblem(NOT_IMPLEMENTED, "You fool of a Took"), "FAILED - Server Error"),
			Arguments.arguments(Problem.valueOf(SEE_OTHER, "Go ahead, make my day"), "FAILED - Unknown Error"));
	}

	@ParameterizedTest
	@MethodSource("provideResponseProblems")
	void sendContentKivraThrowsProblem(final ThrowableProblem problem, final String expectedResult) {
		final var letterEntity = new LetterEntity();
		final var legalId = "1234567890";

		when(kivraClientMock.sendContent(any())).thenThrow(problem);

		final var result = assertDoesNotThrow(() -> kivraIntegration.sendContent(letterEntity, legalId));

		assertThat(result).isEqualTo(expectedResult);
		verify(kivraClientMock).sendContent(any());
		verify(kivraMapperMock).toSendContentRequest(letterEntity, legalId);

	}

	@Test
	void sendContentThrowsException() {
		final var letterEntity = new LetterEntity();
		final var legalId = "1234567890";

		when(kivraClientMock.sendContent(any())).thenThrow(new RuntimeException("Testexception"));

		assertThatThrownBy(() -> kivraIntegration.sendContent(letterEntity, legalId))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while sending content to Kivra for legal id: %s", legalId);

		verify(kivraClientMock).sendContent(any());
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

		when(kivraClientMock.getAllResponses()).thenReturn(keyValues);

		final var result = kivraIntegration.getAllResponses();

		assertThat(result).isNotNull().hasSize(1).allSatisfy(pair -> {
			assertThat(pair.status()).isEqualTo(status);
			assertThat(pair.responseKey()).isEqualTo(responseKey);
		});

	}

	@Test
	void getAllResponsesKivraThrowsServerProblem() {
		when(kivraClientMock.getAllResponses()).thenThrow(new ServerProblem(NOT_IMPLEMENTED, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.getAllResponses())
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Server exception occurred while retrieving Kivra responses");

		verify(kivraClientMock).getAllResponses();
	}

	@Test
	void getAllResponsesKivraThrowsClientProblem() {
		when(kivraClientMock.getAllResponses()).thenThrow(new ClientProblem(BAD_REQUEST, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.getAllResponses())
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while retrieving Kivra responses");

		verify(kivraClientMock).getAllResponses();
	}

	@Test
	void getAllResponsesThrowsException() {
		when(kivraClientMock.getAllResponses()).thenThrow(new RuntimeException("Fasten your seatbelts, it's going to be a bumpy ride"));

		assertThatThrownBy(() -> kivraIntegration.getAllResponses())
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while retrieving Kivra responses");

		verify(kivraClientMock).getAllResponses();
	}

	@Test
	void getRegisteredLetterResponse() {
		final var responseKey = "responseKey";
		final var registeredLetterResponse = RegisteredLetterResponseBuilder.create()
			.withStatus("signed")
			.withSignedAt(NOW)
			.withSenderReference(new RegisteredLetterResponse.SenderReference("internalId"))
			.build();

		when(kivraClientMock.getResponseDetails(responseKey)).thenReturn(registeredLetterResponse);

		final var result = kivraIntegration.getRegisteredLetterResponse(responseKey);

		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo("signed");
		assertThat(result.signedAt()).isEqualTo(NOW);
		assertThat(result.senderReference().internalId()).isEqualTo("internalId");

		verify(kivraClientMock).getResponseDetails(responseKey);
	}

	@Test
	void getRegisteredLetterResponseKivraThrowsServerProblem() {
		final var responseKey = "responseKey";
		when(kivraClientMock.getResponseDetails(responseKey)).thenThrow(new ServerProblem(NOT_IMPLEMENTED, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.getRegisteredLetterResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Exception occurred while retrieving Kivra registered letter response for responseKey: %s", responseKey);

		verify(kivraClientMock).getResponseDetails(responseKey);
	}

	@Test
	void getRegisteredLetterResponseKivraThrowsClientProblem() {
		final var responseKey = "responseKey";
		when(kivraClientMock.getResponseDetails(responseKey)).thenThrow(new ClientProblem(BAD_REQUEST, "Damn you Salazar"));

		assertThatThrownBy(() -> kivraIntegration.getRegisteredLetterResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while retrieving Kivra registered letter response for responseKey: %s", responseKey);

		verify(kivraClientMock).getResponseDetails(responseKey);
	}

	@Test
	void getRegisteredLetterResponseThrowsException() {
		final var responseKey = "responseKey";
		when(kivraClientMock.getResponseDetails(responseKey)).thenThrow(new RuntimeException("Fasten your seatbelts, it's going to be a bumpy ride"));

		assertThatThrownBy(() -> kivraIntegration.getRegisteredLetterResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while retrieving Kivra registered letter response for responseKey: %s", responseKey);

		verify(kivraClientMock).getResponseDetails(responseKey);
	}

	@Test
	void deleteResponse() {
		final var responseKey = "responseKey";

		kivraIntegration.deleteResponse(responseKey);

		verify(kivraClientMock).deleteResponse(responseKey);
	}

	@Test
	void deleteResponseKivraThrowsServerProblem() {
		final var responseKey = "responseKey";
		doThrow(new ServerProblem(NOT_IMPLEMENTED, "Damn you Salazar")).when(kivraClientMock).deleteResponse(responseKey);

		assertThatThrownBy(() -> kivraIntegration.deleteResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Server exception occurred while deleting Kivra response for responseKey: %s", responseKey);

		verify(kivraClientMock).deleteResponse(responseKey);
	}

	@Test
	void deleteResponseKivraThrowsClientProblem() {
		final var responseKey = "responseKey";
		doThrow(new ClientProblem(BAD_REQUEST, "Damn you Salazar")).when(kivraClientMock).deleteResponse(responseKey);

		assertThatThrownBy(() -> kivraIntegration.deleteResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while deleting Kivra response for responseKey: %s", responseKey);

		verify(kivraClientMock).deleteResponse(responseKey);
	}

	@Test
	void deleteResponseThrowsException() {
		final var responseKey = "responseKey";
		doThrow(new RuntimeException("Fasten your seatbelts, it's going to be a bumpy ride")).when(kivraClientMock).deleteResponse(responseKey);

		assertThatThrownBy(() -> kivraIntegration.deleteResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Exception occurred while deleting Kivra response for responseKey: %s", responseKey);

		verify(kivraClientMock).deleteResponse(responseKey);
	}

	@Test
	void healthCheck() {
		kivraIntegration.healthCheck();

		verify(kivraClientMock).getTenantInformation();
	}
}
