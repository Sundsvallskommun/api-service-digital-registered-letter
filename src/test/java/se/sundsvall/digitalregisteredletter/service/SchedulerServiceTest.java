package se.sundsvall.digitalregisteredletter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.TestDataFactory.NOW;
import static se.sundsvall.TestDataFactory.createLetterEntity;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import se.sundsvall.digitalregisteredletter.integration.db.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.dao.LetterRepository;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.KeyValueBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponse;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponseBuilder;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

	@Mock
	private KivraIntegration kivraIntegration;

	@Mock
	private LetterRepository letterRepository;

	@Mock
	private Dept44HealthUtility dept44HealthUtility;

	@Captor
	private ArgumentCaptor<LetterEntity> letterEntityCaptor;

	@InjectMocks
	private SchedulerService schedulerService;

	@Test
	void updateLetterStatuses() {
		var keyValue1 = KeyValueBuilder.create().withResponseKey("responseKey").withStatus("status").build();
		var keyValue2 = KeyValueBuilder.create().withResponseKey("responseKey").withStatus("status2").build();
		var keyValues = List.of(keyValue1, keyValue2);
		var letter = createLetterEntity();
		var status = "signed";

		var registeredLetterResponse = RegisteredLetterResponseBuilder.create()
			.withSignedAt(NOW).withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter.getId())).build();

		when(kivraIntegration.getAllResponses()).thenReturn(keyValues);
		when(kivraIntegration.getRegisteredLetterResponse("responseKey")).thenReturn(registeredLetterResponse);
		when(letterRepository.findByIdAndDeleted(letter.getId(), false)).thenReturn(Optional.of(letter));

		schedulerService.updateLetterStatuses();

		verify(kivraIntegration).getAllResponses();
		verify(kivraIntegration, times(2)).getRegisteredLetterResponse("responseKey");
		verify(letterRepository, times(2)).findByIdAndDeleted(letter.getId(), false);
		verify(letterRepository, times(2)).save(letterEntityCaptor.capture());
		var savedLetter = letterEntityCaptor.getValue();
		assertThat(savedLetter.getStatus()).isEqualTo(status.toUpperCase());
		verify(kivraIntegration, times(2)).deleteResponse("responseKey");
	}

}
