package se.sundsvall.digitalregisteredletter.service.scheduler;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static se.sundsvall.digitalregisteredletter.Constants.STATUS_PENDING;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.digitalregisteredletter.integration.db.LetterRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SigningInformationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.TenantEntity;
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
		final var tenants = letterRepository.findBySigningInformationStatusAndDeletedFalseAndTenantIsNotNull(STATUS_PENDING).stream()
			.map(LetterEntity::getTenant)
			.distinct()
			.toList();
		tenants.forEach(this::processTenant);
	}

	private void processTenant(final TenantEntity tenant) {
		try {
			LOG.info("Processing Kivra responses for tenant with orgNumber: {}", tenant.getOrgNumber());
			ofNullable(kivraIntegration.getAllResponses(tenant.getMunicipalityId(), tenant.getOrgNumber()))
				.orElse(emptyList())
				.forEach(keyValue -> processResponse(keyValue, tenant));
		} catch (final Exception e) {
			LOG.error("Error processing tenant with orgNumber '{}': {}", tenant.getOrgNumber(), e.getMessage(), e);
		}
	}

	private void processResponse(final KeyValue keyValue, final TenantEntity tenant) {
		try {
			final var kivraResponse = kivraIntegration.getRegisteredLetterResponse(keyValue.responseKey(), tenant.getMunicipalityId(), tenant.getOrgNumber());

			final boolean updated = letterRepository.findByIdAndDeleted(kivraResponse.senderReference().internalId(), false)
				.map(letterEntity -> updateLetter(kivraResponse, letterEntity))
				.orElse(false);

			if (updated) {
				removeFromKivra(keyValue, tenant);
			}
		} catch (final Exception e) {
			// Log and swallow exception to not break the execution
			LOG.error("{} thrown when processing kivra response with key '{}'", e.getClass().getSimpleName(), keyValue.responseKey(), e);
		}
	}

	/**
	 * Method updates the letter entity with response values from Kivra. It returns true if the update was successful, false
	 * otherwise.
	 *
	 * @param  kivraResponse the Kivra response to update values from
	 * @param  letterEntity  the letter entity to update
	 * @return               true if the update was successful, false otherwise
	 */
	private boolean updateLetter(final RegisteredLetterResponse kivraResponse, final LetterEntity letterEntity) {
		LOG.info("Updating letter with id '{}'", letterEntity.getId());
		try {
			letterMapper.updateLetterStatus(letterEntity, kivraResponse.status());
			letterMapper.updateSigningInformation(retrieveSigningInformationEntity(letterEntity), kivraResponse);
			letterRepository.save(letterEntity);
		} catch (final Exception e) {
			// Log and return false to not remove a post from kivra
			LOG.error("Error updating letter with id '{}'", letterEntity.getId(), e);
			return false;
		}
		return true;
	}

	/**
	 * Retrieves the SigningInformationEntity from the provided LetterEntity. If no SigningInformationEntity is present, a
	 * new one is created and connected to the LetterEntity.
	 *
	 * @param  letterEntity the letter entity to retrieve the SigningInformationEntity from
	 * @return              the SigningInformationEntity connected to the provided LetterEntity
	 */
	private SigningInformationEntity retrieveSigningInformationEntity(final LetterEntity letterEntity) {
		if (isNull(letterEntity.getSigningInformation())) {
			letterEntity.setSigningInformation(SigningInformationEntity.create());
		}

		return letterEntity.getSigningInformation();
	}

	/**
	 * Removes post in Kivra after a successful update of the letter entity.
	 *
	 * @param keyValue object containing id of post in Kivra
	 * @param tenant   the tenant whose Kivra account the response belongs to
	 */
	private void removeFromKivra(final KeyValue keyValue, final TenantEntity tenant) {
		LOG.info("Deleting kivra response with key '{}'", keyValue.responseKey());
		try {
			kivraIntegration.deleteResponse(keyValue.responseKey(), tenant.getMunicipalityId(), tenant.getOrgNumber());
		} catch (final Exception e) {
			// Log and swallow exception to not break the execution
			LOG.error("Error deleting kivra response with key '{}'", keyValue.responseKey(), e);
		}
	}
}
