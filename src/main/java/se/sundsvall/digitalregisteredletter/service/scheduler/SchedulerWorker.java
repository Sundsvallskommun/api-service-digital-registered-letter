package se.sundsvall.digitalregisteredletter.service.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.digitalregisteredletter.integration.db.dao.LetterRepository;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;

@Component
public class SchedulerWorker {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerWorker.class);

	private final KivraIntegration kivraIntegration;
	private final LetterRepository letterRepository;

	SchedulerWorker(final KivraIntegration kivraIntegration, final LetterRepository letterRepository) {
		this.kivraIntegration = kivraIntegration;
		this.letterRepository = letterRepository;
	}

	public void updateLetterStatuses() {
		for (var keyValue : kivraIntegration.getAllResponses()) {
			var registeredLetterResponse = kivraIntegration.getRegisteredLetterResponse(keyValue.responseKey());
			var letterId = registeredLetterResponse.senderReference().internalId();
			var optionalLetter = letterRepository.findByIdAndDeleted(letterId, false);
			if (optionalLetter.isPresent()) {
				var letter = optionalLetter.get();
				LOG.info("Updating letter with id '{}' status to '{}'", letterId, keyValue.status());
				letter.setStatus(registeredLetterResponse.status().toUpperCase());
				letterRepository.save(letter);
			}
			LOG.info("Deleting response with key '{}'", keyValue.responseKey());
			kivraIntegration.deleteResponse(keyValue.responseKey());
		}
	}

}
