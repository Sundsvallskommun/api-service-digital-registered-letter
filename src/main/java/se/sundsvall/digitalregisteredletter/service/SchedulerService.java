package se.sundsvall.digitalregisteredletter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import se.sundsvall.digitalregisteredletter.integration.db.dao.LetterRepository;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;

@Service
public class SchedulerService {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

	private final KivraIntegration kivraIntegration;
	private final LetterRepository letterRepository;
	private final Dept44HealthUtility dept44HealthUtility;

	public SchedulerService(final KivraIntegration kivraIntegration,
		final LetterRepository letterRepository,
		final Dept44HealthUtility dept44HealthUtility) {
		this.kivraIntegration = kivraIntegration;
		this.letterRepository = letterRepository;
		this.dept44HealthUtility = dept44HealthUtility;
	}

	/**
	 * Scheduled task to update letter statuses based on responses from Kivra. When the status of a letter is updated, the
	 * corresponding response in Kivra is deleted.
	 */
	@Dept44Scheduled(
		cron = "${scheduler.update-letter-statuses.cron}",
		name = "${scheduler.update-letter-statuses.name}",
		lockAtMostFor = "${scheduler.suspension.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.suspension.maximum-execution-time}")
	void updateLetterStatuses() {
		for (var keyValue : kivraIntegration.getAllResponses()) {
			var registeredLetterResponse = kivraIntegration.getRegisteredLetterResponse(keyValue.responseKey());
			var letterId = registeredLetterResponse.senderReference().internalId();
			var optionalLetter = letterRepository.findByIdAndDeleted(letterId, false);
			if (optionalLetter.isPresent()) {
				var letter = optionalLetter.get();
				LOG.info("Updating letter with id '{}' status to '{}'", letterId, keyValue.status());
				letter.setStatus(registeredLetterResponse.status().toUpperCase());
				letterRepository.save(letter);
				kivraIntegration.deleteResponse(keyValue.responseKey());
			} else {
				LOG.info("Found no letter with id '{}'", letterId);
				dept44HealthUtility.setHealthIndicatorUnhealthy("updateLetterStatuses", "Kivra returned an internal id '%s' that does not match any letter in the database.".formatted(letterId));
			}
		}
	}
}
