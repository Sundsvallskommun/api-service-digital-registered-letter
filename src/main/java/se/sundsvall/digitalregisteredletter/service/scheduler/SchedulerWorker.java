package se.sundsvall.digitalregisteredletter.service.scheduler;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.digitalregisteredletter.integration.db.LetterRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SigningInformationEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.KeyValue;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponse;
import se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper;

@Component
public class SchedulerWorker {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerWorker.class);

	private final KivraIntegration kivraIntegration;
	private final LetterRepository letterRepository;
	private final LetterMapper letterMapper;

	SchedulerWorker(
		final KivraIntegration kivraIntegration,
		final LetterRepository letterRepository,
		final LetterMapper letterMapper) {

		this.kivraIntegration = kivraIntegration;
		this.letterRepository = letterRepository;
		this.letterMapper = letterMapper;
	}

	public void updateLetterInformation() {
		ofNullable(kivraIntegration.getAllResponses()).orElse(emptyList()).stream()
			.forEach(this::processResponse);
	}

	private void processResponse(final KeyValue keyValue) {
		try {
			final var kivraResponse = kivraIntegration.getRegisteredLetterResponse(keyValue.responseKey());

			final var updated = letterRepository.findByIdAndDeleted(kivraResponse.senderReference().internalId(), false)
				.map(letterEntity -> updateLetter(kivraResponse, letterEntity))
				.orElse(false);

			if (TRUE.equals(updated)) {
				removeFromKivra(keyValue);
			}
		} catch (final Exception e) {
			// Log and swollow exception to not break the execution
			LOG.error("{} thrown when processing kivra response with key '{}'", e.getClass().getSimpleName(), keyValue.responseKey(), e);
		}
	}

	/**
	 * Method updates letter entity with response values from Kivra. It returns true if update was successful, false
	 * otherwise.
	 *
	 * @param  kivraResponse the Kivra response to update values from
	 * @param  letterEntity  the letter entity to update
	 * @return               true if update was successful, false otherwise
	 */
	private boolean updateLetter(final RegisteredLetterResponse kivraResponse, final LetterEntity letterEntity) {
		LOG.info("Updating letter with id '{}'", letterEntity.getId());
		try {
			letterMapper.updateLetterStatus(letterEntity, kivraResponse.status());
			letterMapper.updateSigningInformation(retrieveSigningInfoformationEntity(letterEntity), kivraResponse);
			letterRepository.save(letterEntity);
		} catch (final Exception e) {
			// Log and return false to not remove post from kivra
			LOG.error("Error updating letter with id '{}'", letterEntity.getId(), e);
			return false;
		}
		return true;
	}

	/**
	 * Method for retrieving the SigningInformationEntity from sent in LetterEntity object. It returns the
	 * SigningInformationEntity connected to the sent in LetterEntity with the logic to create and connect a new
	 * SigningInformationEntity if it is not present within the provided LetterEntity.
	 *
	 * @param  letterEntity the letter entity to retrieve the SigningInformationEntity from
	 * @return              the SigningInformationEntity connected to the provided LetterEntity
	 */
	private SigningInformationEntity retrieveSigningInfoformationEntity(LetterEntity letterEntity) {
		if (isNull(letterEntity.getSigningInformation())) {
			letterEntity.setSigningInformation(SigningInformationEntity.create());
		}

		return letterEntity.getSigningInformation();
	}

	/**
	 * Method removes post in kivra and is used to tidy up after successful update of letter entity
	 *
	 * @param keyValue object containing id of post in Kivra
	 */
	private void removeFromKivra(final KeyValue keyValue) {
		LOG.info("Deleting kivra response with key '{}'", keyValue.responseKey());
		try {
			kivraIntegration.deleteResponse(keyValue.responseKey());
		} catch (final Exception e) {
			// Log and swollow exception to not break the execution
			LOG.error("Error deleting kivra response with key '{}'", keyValue.responseKey(), e);
		}
	}
}
