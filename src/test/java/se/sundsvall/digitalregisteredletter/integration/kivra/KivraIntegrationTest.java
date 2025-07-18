package se.sundsvall.digitalregisteredletter.integration.kivra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
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
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.integration.db.LetterEntity;
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
		var legalIds = List.of("1234567890");
		var userMatchV2SSN = new UserMatchV2SSN(legalIds);
		var requestCaptor = ArgumentCaptor.forClass(UserMatchV2SSN.class);

		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(userMatchV2SSN);
		when(kivraClientMock.checkEligibility(requestCaptor.capture())).thenReturn(userMatchV2SSN);

		var result = kivraIntegration.checkEligibility(legalIds);
		assertThat(result).isNotNull().hasSize(1).containsExactly(legalIds.getFirst());

		var capturedRequest = requestCaptor.getValue();
		assertThat(capturedRequest).isNotNull();
		assertThat(capturedRequest.legalIds()).hasSize(1).containsExactly(legalIds.getFirst());

		verify(kivraMapperMock).toCheckEligibilityRequest(legalIds);
		verify(kivraClientMock).checkEligibility(capturedRequest);
	}

	@Test
	void checkEligibilityKivraThrows() {
		var legalId = "1234567890";
		var legalIds = List.of(legalId);
		var request = new UserMatchV2SSN(legalIds);
		when(kivraMapperMock.toCheckEligibilityRequest(legalIds)).thenReturn(request);
		when(kivraClientMock.checkEligibility(request)).thenThrow(new RuntimeException("Kivra service error"));

		assertThatThrownBy(() -> kivraIntegration.checkEligibility(legalIds))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Exception occurred while checking Kivra eligibility for legal ids: %s", legalIds);

		verify(kivraClientMock).checkEligibility(request);
	}

	@ParameterizedTest
	@MethodSource("provideSendContentResponses")
	void sendContentTest(final Integer statusCode, final String expectedStatus) {
		var letterEntity = new LetterEntity();
		var legalId = "1234567890";
		var response = ResponseEntity.status(statusCode).body(ContentUserBuilder.create()
			.withSubject("subject")
			.withGeneratedAt(LocalDate.MIN)
			.withType("registered.letter")
			.build());

		var requestCaptor = ArgumentCaptor.forClass(ContentUserV2.class);
		when(kivraMapperMock.toSendContentRequest(letterEntity, legalId)).thenCallRealMethod();
		when(kivraClientMock.sendContent(requestCaptor.capture())).thenReturn(response);

		var result = kivraIntegration.sendContent(letterEntity, legalId);

		assertThat(result).isNotNull().isInstanceOf(String.class).isEqualTo(expectedStatus);

		var capturedRequest = requestCaptor.getValue();
		assertThat(capturedRequest).isNotNull().isInstanceOf(ContentUserV2.class);
		assertThat(capturedRequest.subject()).isEqualTo(letterEntity.getSubject());
		assertThat(capturedRequest.legalId()).isEqualTo(legalId);
		assertThat(capturedRequest.type()).isEqualTo("registered.letter");

		verify(kivraMapperMock).toSendContentRequest(letterEntity, legalId);
		verify(kivraMapperMock).toRegisteredLetter(letterEntity.getId());
		verify(kivraMapperMock).toPartsResponsives(letterEntity.getAttachments());
		verify(kivraClientMock).sendContent(capturedRequest);
	}

	private static Stream<Arguments> provideSendContentResponses() {
		return Stream.of(
			Arguments.of(200, "SENT"),
			Arguments.of(400, "FAILED - Client Error"),
			Arguments.of(500, "FAILED - Server Error"),
			Arguments.arguments(100, "FAILED - Unknown Error"));
	}

	@Test
	void sendContentKivraThrows() {
		var letterEntity = new LetterEntity();
		var legalId = "1234567890";
		when(kivraClientMock.sendContent(any())).thenThrow(new RuntimeException("Kivra service error"));

		assertThatThrownBy(() -> kivraIntegration.sendContent(letterEntity, legalId))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Exception occurred while sending content to Kivra for legal id: %s", legalId);

		verify(kivraClientMock).sendContent(any());
		verify(kivraMapperMock).toSendContentRequest(letterEntity, legalId);
	}

	@Test
	void getAllResponses() {
		var status = "signed";
		var responseKey = "responseKey";
		var keyValue = KeyValueBuilder.create()
			.withStatus(status)
			.withResponseKey(responseKey)
			.build();
		var keyValues = List.of(keyValue);

		when(kivraClientMock.getAllResponses()).thenReturn(keyValues);

		var result = kivraIntegration.getAllResponses();

		assertThat(result).isNotNull().hasSize(1).allSatisfy(pair -> {
			assertThat(pair.status()).isEqualTo(status);
			assertThat(pair.responseKey()).isEqualTo(responseKey);
		});

	}

	@Test
	void getAllResponsesKivraThrows() {
		when(kivraClientMock.getAllResponses()).thenThrow(new RuntimeException("Kivra service error"));

		assertThatThrownBy(() -> kivraIntegration.getAllResponses())
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Exception occurred while retrieving Kivra responses");

		verify(kivraClientMock).getAllResponses();
	}

	@Test
	void getRegisteredLetterResponse() {
		var responseKey = "responseKey";
		var registeredLetterResponse = RegisteredLetterResponseBuilder.create()
			.withStatus("signed")
			.withSignedAt(NOW)
			.withSenderReference(new RegisteredLetterResponse.SenderReference("internalId"))
			.build();

		when(kivraClientMock.getResponseDetails(responseKey)).thenReturn(registeredLetterResponse);

		var result = kivraIntegration.getRegisteredLetterResponse(responseKey);

		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo("signed");
		assertThat(result.signedAt()).isEqualTo(NOW);
		assertThat(result.senderReference().internalId()).isEqualTo("internalId");

		verify(kivraClientMock).getResponseDetails(responseKey);
	}

	@Test
	void getRegisteredLetterResponseKivraThrows() {
		var responseKey = "responseKey";
		when(kivraClientMock.getResponseDetails(responseKey)).thenThrow(new RuntimeException("Kivra service error"));

		assertThatThrownBy(() -> kivraIntegration.getRegisteredLetterResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Exception occurred while retrieving Kivra registered letter response for responseKey: %s", responseKey);

		verify(kivraClientMock).getResponseDetails(responseKey);
	}

	@Test
	void deleteResponse() {
		var responseKey = "responseKey";

		kivraIntegration.deleteResponse(responseKey);

		verify(kivraClientMock).deleteResponse(responseKey);
	}

	@Test
	void deleteResponseKivraThrows() {
		var responseKey = "responseKey";
		when(kivraClientMock.deleteResponse(responseKey)).thenThrow(new RuntimeException("Kivra service error"));

		assertThatThrownBy(() -> kivraIntegration.deleteResponse(responseKey))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Exception occurred while deleting Kivra response for responseKey: %s", responseKey);

		verify(kivraClientMock).deleteResponse(responseKey);
	}
}
