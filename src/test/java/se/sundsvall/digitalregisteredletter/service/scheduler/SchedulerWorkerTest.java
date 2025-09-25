package se.sundsvall.digitalregisteredletter.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.TestDataFactory.NOW;
import static se.sundsvall.TestDataFactory.createLetterEntity;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.digitalregisteredletter.integration.db.LetterRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SigningInformationEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.KeyValueBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponse;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponseBuilder;

@ExtendWith(MockitoExtension.class)
class SchedulerWorkerTest {

	@Mock
	private KivraIntegration kivraIntegrationMock;

	@Mock
	private LetterRepository letterRepositoryMock;

	@Captor
	private ArgumentCaptor<LetterEntity> letterEntityCaptor;

	@InjectMocks
	private SchedulerWorker schedulerWorker;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(kivraIntegrationMock, letterRepositoryMock);
	}

	@Test
	void updateLetterInformation() {
		final var keyValue1 = KeyValueBuilder.create().withResponseKey("responseKey").withStatus("status").build();
		final var keyValue2 = KeyValueBuilder.create().withResponseKey("responseKey").withStatus("status2").build();
		final var keyValues = List.of(keyValue1, keyValue2);
		final var letter = createLetterEntity();
		final var status = "signed";
		final var registeredLetterResponse = RegisteredLetterResponseBuilder.create().withSignedAt(NOW).withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter.getId())).build();

		when(kivraIntegrationMock.getAllResponses()).thenReturn(keyValues);
		when(kivraIntegrationMock.getRegisteredLetterResponse("responseKey")).thenReturn(registeredLetterResponse);
		when(letterRepositoryMock.findByIdAndDeleted(letter.getId(), false)).thenReturn(Optional.of(letter));

		schedulerWorker.updateLetterInformation();

		verify(kivraIntegrationMock).getAllResponses();
		verify(kivraIntegrationMock, times(2)).getRegisteredLetterResponse("responseKey");
		verify(letterRepositoryMock, times(2)).findByIdAndDeleted(letter.getId(), false);
		verify(letterRepositoryMock, times(2)).save(letterEntityCaptor.capture());
		verify(kivraIntegrationMock, times(2)).deleteResponse("responseKey");

		letterEntityCaptor.getAllValues().stream().forEach(savedLetter -> {
			assertThat(savedLetter.getStatus()).isEqualTo(status.toUpperCase());
		});
	}

	/*
	 * Tests below verifies that loop is not interrupted by a single entity update failure
	 */

	@Test
	void updateLetterInformationWhenUnknownExceptionThrown() {
		final var keyValue1 = KeyValueBuilder.create().withResponseKey("responseKey1").build();
		final var keyValue2 = KeyValueBuilder.create().withResponseKey("responseKey2").build();
		final var keyValues = List.of(keyValue1, keyValue2);
		final var letter = createLetterEntity().withId("responseKey2");
		final var status = "signed";
		final var registeredLetterResponse = RegisteredLetterResponseBuilder.create().withSignedAt(NOW).withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter.getId())).build();

		when(kivraIntegrationMock.getAllResponses()).thenReturn(keyValues);
		when(kivraIntegrationMock.getRegisteredLetterResponse("responseKey1")).thenThrow(Problem.valueOf(Status.I_AM_A_TEAPOT, "Test exception"));
		when(kivraIntegrationMock.getRegisteredLetterResponse("responseKey2")).thenReturn(registeredLetterResponse);

		when(letterRepositoryMock.findByIdAndDeleted(letter.getId(), false)).thenReturn(Optional.of(letter));

		schedulerWorker.updateLetterInformation();

		verify(kivraIntegrationMock).getAllResponses();
		verify(kivraIntegrationMock).getRegisteredLetterResponse("responseKey1");
		verify(kivraIntegrationMock).getRegisteredLetterResponse("responseKey2");
		verify(letterRepositoryMock).findByIdAndDeleted(letter.getId(), false);
		verify(letterRepositoryMock).save(letter);
		verify(kivraIntegrationMock).deleteResponse("responseKey2");
	}

	@Test
	void updateLetterInformationWhenRepositorySaveThrowsException() {
		final var keyValue1 = KeyValueBuilder.create().withResponseKey("responseKey1").build();
		final var keyValue2 = KeyValueBuilder.create().withResponseKey("responseKey2").build();
		final var keyValues = List.of(keyValue1, keyValue2);
		final var signingInformation = SigningInformationEntity.create();
		final var letter1 = createLetterEntity().withId("responseKey1");
		final var letter2 = createLetterEntity().withId("responseKey2").withSigningInformation(signingInformation);
		final var status = "signed";
		final var registeredLetterResponse1 = RegisteredLetterResponseBuilder.create().withSignedAt(NOW).withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter1.getId())).build();
		final var registeredLetterResponse2 = RegisteredLetterResponseBuilder.create().withSignedAt(NOW).withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter2.getId())).build();

		when(kivraIntegrationMock.getAllResponses()).thenReturn(keyValues);
		when(kivraIntegrationMock.getRegisteredLetterResponse("responseKey1")).thenReturn(registeredLetterResponse1);
		when(kivraIntegrationMock.getRegisteredLetterResponse("responseKey2")).thenReturn(registeredLetterResponse2);
		when(letterRepositoryMock.findByIdAndDeleted(letter1.getId(), false)).thenReturn(Optional.of(letter1));
		when(letterRepositoryMock.findByIdAndDeleted(letter2.getId(), false)).thenReturn(Optional.of(letter2));
		when(letterRepositoryMock.save(letter1)).thenThrow(Problem.valueOf(Status.I_AM_A_TEAPOT, "Test exception"));
		when(letterRepositoryMock.save(letter2)).thenReturn(letter2);

		schedulerWorker.updateLetterInformation();

		verify(kivraIntegrationMock).getAllResponses();
		verify(kivraIntegrationMock).getRegisteredLetterResponse("responseKey1");
		verify(kivraIntegrationMock).getRegisteredLetterResponse("responseKey2");
		verify(letterRepositoryMock).findByIdAndDeleted(letter1.getId(), false);
		verify(letterRepositoryMock).findByIdAndDeleted(letter2.getId(), false);
		verify(letterRepositoryMock).save(letter1);
		verify(letterRepositoryMock).save(letter2);
		verify(kivraIntegrationMock).deleteResponse("responseKey2");

		assertThat(letter2.getSigningInformation()).isSameAs(signingInformation);
	}

	@Test
	void updateLetterInformationKivraRemovalThrowsException() {
		final var keyValue1 = KeyValueBuilder.create().withResponseKey("responseKey1").build();
		final var keyValue2 = KeyValueBuilder.create().withResponseKey("responseKey2").build();
		final var keyValues = List.of(keyValue1, keyValue2);
		final var letter1 = createLetterEntity().withId("responseKey1");
		final var letter2 = createLetterEntity().withId("responseKey2");
		final var status = "signed";
		final var registeredLetterResponse1 = RegisteredLetterResponseBuilder.create().withSignedAt(NOW).withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter1.getId())).build();
		final var registeredLetterResponse2 = RegisteredLetterResponseBuilder.create().withSignedAt(NOW).withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter2.getId())).build();

		when(kivraIntegrationMock.getAllResponses()).thenReturn(keyValues);
		when(kivraIntegrationMock.getRegisteredLetterResponse("responseKey1")).thenReturn(registeredLetterResponse1);
		when(kivraIntegrationMock.getRegisteredLetterResponse("responseKey2")).thenReturn(registeredLetterResponse2);
		when(letterRepositoryMock.findByIdAndDeleted(letter1.getId(), false)).thenReturn(Optional.of(letter1));
		when(letterRepositoryMock.findByIdAndDeleted(letter2.getId(), false)).thenReturn(Optional.of(letter2));
		when(letterRepositoryMock.save(letter1)).thenReturn(letter2);
		when(letterRepositoryMock.save(letter2)).thenReturn(letter2);
		doThrow(Problem.valueOf(Status.I_AM_A_TEAPOT, "Test exception")).when(kivraIntegrationMock).deleteResponse(letter1.getId());

		schedulerWorker.updateLetterInformation();

		verify(kivraIntegrationMock).getAllResponses();
		verify(kivraIntegrationMock).getRegisteredLetterResponse("responseKey1");
		verify(kivraIntegrationMock).getRegisteredLetterResponse("responseKey2");
		verify(letterRepositoryMock).findByIdAndDeleted(letter1.getId(), false);
		verify(letterRepositoryMock).findByIdAndDeleted(letter2.getId(), false);
		verify(letterRepositoryMock).save(letter1);
		verify(letterRepositoryMock).save(letter2);
		verify(kivraIntegrationMock).deleteResponse("responseKey1");
		verify(kivraIntegrationMock).deleteResponse("responseKey2");
	}
}
