package se.sundsvall.digitalregisteredletter.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
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
import se.sundsvall.digitalregisteredletter.integration.db.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.dao.LetterRepository;
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
	void updateLetterStatuses() {
		var keyValue1 = KeyValueBuilder.create().withResponseKey("responseKey").withStatus("status").build();
		var keyValue2 = KeyValueBuilder.create().withResponseKey("responseKey").withStatus("status2").build();
		var keyValues = List.of(keyValue1, keyValue2);
		var letter = createLetterEntity();
		var status = "signed";

		var registeredLetterResponse = RegisteredLetterResponseBuilder.create()
			.withSignedAt(NOW).withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter.getId())).build();

		when(kivraIntegrationMock.getAllResponses()).thenReturn(keyValues);
		when(kivraIntegrationMock.getRegisteredLetterResponse("responseKey")).thenReturn(registeredLetterResponse);
		when(letterRepositoryMock.findByIdAndDeleted(letter.getId(), false)).thenReturn(Optional.of(letter));

		schedulerWorker.updateLetterStatuses();

		verify(kivraIntegrationMock).getAllResponses();
		verify(kivraIntegrationMock, times(2)).getRegisteredLetterResponse("responseKey");
		verify(letterRepositoryMock, times(2)).findByIdAndDeleted(letter.getId(), false);
		verify(letterRepositoryMock, times(2)).save(letterEntityCaptor.capture());
		var savedLetter = letterEntityCaptor.getValue();
		assertThat(savedLetter.getStatus()).isEqualTo(status.toUpperCase());
		verify(kivraIntegrationMock, times(2)).deleteResponse("responseKey");
	}
}
