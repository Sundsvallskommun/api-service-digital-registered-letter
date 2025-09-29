package se.sundsvall.digitalregisteredletter.service.scheduler;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.TestDataFactory.createLetterEntity;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.digitalregisteredletter.integration.db.LetterRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.SigningInformationEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.KeyValueBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponse;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponseBuilder;
import se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper;

@ExtendWith(MockitoExtension.class)
class SchedulerWorkerTest {

	@Mock
	private KivraIntegration kivraIntegrationMock;

	@Mock
	private LetterRepository letterRepositoryMock;

	@Mock
	private LetterMapper letterMapperMock;

	@InjectMocks
	private SchedulerWorker schedulerWorker;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(kivraIntegrationMock, letterRepositoryMock, letterMapperMock);
	}

	@Test
	void updateLetterInformation() {
		final var existingSigningInformation = SigningInformationEntity.create();
		final var keyValue1 = KeyValueBuilder.create().withResponseKey("letterId1").withStatus("status1").build();
		final var keyValue2 = KeyValueBuilder.create().withResponseKey("letterId2").withStatus("status2").build();
		final var keyValues = List.of(keyValue1, keyValue2);
		final var letter1 = createLetterEntity().withId("letterId1");
		final var letter2 = createLetterEntity().withId("letterId2").withSigningInformation(existingSigningInformation);
		final var status = "signed";
		final var registeredLetterResponse1 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter1.getId())).build();
		final var registeredLetterResponse2 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter2.getId())).build();

		when(kivraIntegrationMock.getAllResponses()).thenReturn(keyValues);
		when(kivraIntegrationMock.getRegisteredLetterResponse(letter1.getId())).thenReturn(registeredLetterResponse1);
		when(kivraIntegrationMock.getRegisteredLetterResponse(letter2.getId())).thenReturn(registeredLetterResponse2);
		when(letterRepositoryMock.findByIdAndDeleted(letter1.getId(), false)).thenReturn(Optional.of(letter1));
		when(letterRepositoryMock.findByIdAndDeleted(letter2.getId(), false)).thenReturn(Optional.of(letter2));

		schedulerWorker.updateLetterInformation();

		verify(kivraIntegrationMock).getAllResponses();
		verify(kivraIntegrationMock).getRegisteredLetterResponse(letter1.getId());
		verify(kivraIntegrationMock).getRegisteredLetterResponse(letter2.getId());
		verify(letterRepositoryMock).findByIdAndDeleted(letter1.getId(), false);
		verify(letterRepositoryMock).findByIdAndDeleted(letter2.getId(), false);
		verify(letterRepositoryMock).save(letter1);
		verify(letterRepositoryMock).save(letter2);
		verify(kivraIntegrationMock).deleteResponse(letter1.getId());
		verify(kivraIntegrationMock).deleteResponse(letter2.getId());
		verify(letterMapperMock).updateLetterStatus(letter1, status);
		verify(letterMapperMock).updateLetterStatus(letter2, status);
		verify(letterMapperMock).updateSigningInformation(letter1.getSigningInformation(), registeredLetterResponse1);
		verify(letterMapperMock).updateSigningInformation(letter2.getSigningInformation(), registeredLetterResponse2);
	}

	/*
	 * Tests below verifies that loop is not interrupted by a single entity update failure
	 */

	@Test
	void updateLetterInformationWhenUnknownExceptionThrown() {
		final var keyValue1 = KeyValueBuilder.create().withResponseKey("letterId1").build();
		final var keyValue2 = KeyValueBuilder.create().withResponseKey("letterId2").build();
		final var keyValues = List.of(keyValue1, keyValue2);
		final var letter = createLetterEntity().withId("letterId2");
		final var status = "signed";
		final var registeredLetterResponse = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter.getId())).build();

		when(kivraIntegrationMock.getAllResponses()).thenReturn(keyValues);
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId1")).thenThrow(Problem.valueOf(Status.I_AM_A_TEAPOT, "Test exception"));
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId2")).thenReturn(registeredLetterResponse);
		when(letterRepositoryMock.findByIdAndDeleted(letter.getId(), false)).thenReturn(Optional.of(letter));

		schedulerWorker.updateLetterInformation();

		verify(kivraIntegrationMock).getAllResponses();
		verify(kivraIntegrationMock).getRegisteredLetterResponse("letterId1");
		verify(kivraIntegrationMock).getRegisteredLetterResponse("letterId2");
		verify(letterRepositoryMock).findByIdAndDeleted(letter.getId(), false);
		verify(letterRepositoryMock).save(letter);
		verify(kivraIntegrationMock).deleteResponse("letterId2");
		verify(letterMapperMock).updateLetterStatus(letter, status);
		verify(letterMapperMock).updateSigningInformation(letter.getSigningInformation(), registeredLetterResponse);
	}

	@Test
	void updateLetterInformationWhenRepositorySaveThrowsException() {
		final var keyValue1 = KeyValueBuilder.create().withResponseKey("letterId1").build();
		final var keyValue2 = KeyValueBuilder.create().withResponseKey("letterId2").build();
		final var keyValues = List.of(keyValue1, keyValue2);
		final var letter1 = createLetterEntity().withId("letterId1");
		final var letter2 = createLetterEntity().withId("letterId2");
		final var status = "signed";
		final var registeredLetterResponse1 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter1.getId())).build();
		final var registeredLetterResponse2 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter2.getId())).build();

		when(kivraIntegrationMock.getAllResponses()).thenReturn(keyValues);
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId1")).thenReturn(registeredLetterResponse1);
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId2")).thenReturn(registeredLetterResponse2);
		when(letterRepositoryMock.findByIdAndDeleted(letter1.getId(), false)).thenReturn(Optional.of(letter1));
		when(letterRepositoryMock.findByIdAndDeleted(letter2.getId(), false)).thenReturn(Optional.of(letter2));
		when(letterRepositoryMock.save(letter1)).thenThrow(Problem.valueOf(Status.I_AM_A_TEAPOT, "Test exception"));
		when(letterRepositoryMock.save(letter2)).thenReturn(letter2);

		schedulerWorker.updateLetterInformation();

		verify(kivraIntegrationMock).getAllResponses();
		verify(kivraIntegrationMock).getRegisteredLetterResponse(letter1.getId());
		verify(kivraIntegrationMock).getRegisteredLetterResponse(letter2.getId());
		verify(letterRepositoryMock).findByIdAndDeleted(letter1.getId(), false);
		verify(letterRepositoryMock).findByIdAndDeleted(letter2.getId(), false);
		verify(letterRepositoryMock).save(letter1);
		verify(letterRepositoryMock).save(letter2);
		verify(kivraIntegrationMock).deleteResponse(letter2.getId());
		verify(letterMapperMock).updateLetterStatus(letter1, status);
		verify(letterMapperMock).updateLetterStatus(letter2, status);
		verify(letterMapperMock).updateSigningInformation(letter1.getSigningInformation(), registeredLetterResponse1);
		verify(letterMapperMock).updateSigningInformation(letter2.getSigningInformation(), registeredLetterResponse2);
	}

	@Test
	void updateLetterInformationKivraRemovalThrowsException() {
		final var keyValue1 = KeyValueBuilder.create().withResponseKey("letterId1").build();
		final var keyValue2 = KeyValueBuilder.create().withResponseKey("letterId2").build();
		final var keyValues = List.of(keyValue1, keyValue2);
		final var letter1 = createLetterEntity().withId("letterId1");
		final var letter2 = createLetterEntity().withId("letterId2");
		final var status = "signed";
		final var registeredLetterResponse1 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter1.getId())).build();
		final var registeredLetterResponse2 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter2.getId())).build();

		when(kivraIntegrationMock.getAllResponses()).thenReturn(keyValues);
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId1")).thenReturn(registeredLetterResponse1);
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId2")).thenReturn(registeredLetterResponse2);
		when(letterRepositoryMock.findByIdAndDeleted(letter1.getId(), false)).thenReturn(Optional.of(letter1));
		when(letterRepositoryMock.findByIdAndDeleted(letter2.getId(), false)).thenReturn(Optional.of(letter2));
		when(letterRepositoryMock.save(letter1)).thenReturn(letter2);
		when(letterRepositoryMock.save(letter2)).thenReturn(letter2);
		doThrow(Problem.valueOf(Status.I_AM_A_TEAPOT, "Test exception")).when(kivraIntegrationMock).deleteResponse(letter1.getId());

		schedulerWorker.updateLetterInformation();

		verify(kivraIntegrationMock).getAllResponses();
		verify(kivraIntegrationMock).getRegisteredLetterResponse(letter1.getId());
		verify(kivraIntegrationMock).getRegisteredLetterResponse(letter2.getId());
		verify(letterRepositoryMock).findByIdAndDeleted(letter1.getId(), false);
		verify(letterRepositoryMock).findByIdAndDeleted(letter2.getId(), false);
		verify(letterRepositoryMock).save(letter1);
		verify(letterRepositoryMock).save(letter2);
		verify(kivraIntegrationMock).deleteResponse(letter1.getId());
		verify(kivraIntegrationMock).deleteResponse(letter2.getId());
		verify(letterMapperMock).updateLetterStatus(letter1, status);
		verify(letterMapperMock).updateLetterStatus(letter2, status);
		verify(letterMapperMock).updateSigningInformation(letter1.getSigningInformation(), registeredLetterResponse1);
		verify(letterMapperMock).updateSigningInformation(letter2.getSigningInformation(), registeredLetterResponse2);
	}
}
