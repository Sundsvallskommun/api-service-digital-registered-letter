package se.sundsvall.digitalregisteredletter.service.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

	@Mock
	private SchedulerWorker schedulerWorkerMock;

	@InjectMocks
	private SchedulerService schedulerService;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(schedulerWorkerMock);
	}

	@Test
	void updateLetterStatuses() {
		schedulerService.updateLetterStatuses();

		verify(schedulerWorkerMock).updateLetterInformation();
	}
}
