package se.sundsvall.digitalregisteredletter.service.scheduler;

import org.springframework.stereotype.Service;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@Service
public class SchedulerService {

	private final SchedulerWorker schedulerWorker;

	SchedulerService(final SchedulerWorker schedulerWorker) {
		this.schedulerWorker = schedulerWorker;
	}

	/**
	 * When a digital registered letter is opened or expires, a response is created at Kivra. When we fetch all the
	 * available responses for our tenantId, Kivra returns a list of key-value pairs. The key is a responseKey and the value
	 * is the status of the response. We need to fetch the complete response from Kivra using the responseKey, the complete
	 * response contains a
	 * sender_reference object that has a sender_internal_id that corresponds to a letters primary key in our database. We
	 * use the sender_internal_id to fetch the letter from our database and update its status based on the response from
	 * Kivra.
	 * After updating the letter, we delete the response from Kivra to ensure that the same response is not processed
	 * multiple times.
	 */
	@Dept44Scheduled(
		cron = "${scheduler.update-letter-statuses.cron}",
		name = "${scheduler.update-letter-statuses.name}",
		lockAtMostFor = "${scheduler.update-letter-statuses.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.update-letter-statuses.maximum-execution-time}")
	void updateLetterStatuses() {
		schedulerWorker.updateLetterStatuses();
	}
}
