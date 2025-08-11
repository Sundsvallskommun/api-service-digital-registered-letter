package se.sundsvall.digitalregisteredletter.integration.kivra;

import static java.util.Collections.emptyList;
import static org.zalando.problem.Status.BAD_GATEWAY;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.integration.db.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.KeyValue;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponse;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.UserMatchV2SSN;

@Component
public class KivraIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(KivraIntegration.class);
	private final KivraClient kivraClient;
	private final KivraMapper kivraMapper;

	public KivraIntegration(final KivraClient kivraClient,
		final KivraMapper kivraMapper) {
		this.kivraClient = kivraClient;
		this.kivraMapper = kivraMapper;
	}

	/**
	 * Checks if the provided legal IDs are eligible for Kivra.
	 *
	 * @param  legalIds List of legal IDs to check eligibility for.
	 * @return          List of legal IDs that are eligible for Kivra.
	 */
	public List<String> checkEligibility(final List<String> legalIds) {
		try {
			var request = kivraMapper.toCheckEligibilityRequest(legalIds);
			LOG.info("Checking Kivra eligibility for legal ids: {}", legalIds);
			var response = kivraClient.checkEligibility(request);
			LOG.info("Kivra eligibility check successful");
			return Optional.ofNullable(response).map(UserMatchV2SSN::legalIds).orElse(emptyList());
		} catch (Exception e) {
			LOG.error("Exception occurred when checking Kivra eligibility for legal ids: {}, exception message: {}", legalIds, e.getMessage(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Exception occurred while checking Kivra eligibility for legal ids: " + legalIds);
		}
	}

	/**
	 * Sends content to Kivra for the specified letter entity and legal ID.
	 *
	 * @param  letterEntity The letter entity containing the content to be sent.
	 * @param  legalId      The legal ID of the recipient.
	 * @return              The status that indicates whether the content was sent successfully or if there was an error.
	 */
	public String sendContent(final LetterEntity letterEntity, final String legalId) {
		try {
			var request = kivraMapper.toSendContentRequest(letterEntity, legalId);
			LOG.info("Sending content to Kivra for legal id: {}", legalId);
			var response = kivraClient.sendContent(request);
			LOG.info("Kivra content sent successfully for legal id: {}", legalId);

			if (response.getStatusCode().is2xxSuccessful()) {
				return "SENT";
			} else if (response.getStatusCode().is4xxClientError()) {
				return "FAILED - Client Error";
			} else if (response.getStatusCode().is5xxServerError()) {
				return "FAILED - Server Error";
			} else {
				return "FAILED - Unknown Error";
			}
		} catch (Exception e) {
			LOG.error("Exception occurred when sending content to Kivra for legal id: {}, exception message: {}", legalId, e.getMessage(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Exception occurred while sending content to Kivra for legal id: " + legalId);
		}
	}

	/**
	 * Retrieves all responses from Kivra.
	 *
	 * @return List of KeyValue objects representing the responses.
	 */
	public List<KeyValue> getAllResponses() {
		try {
			LOG.info("Retrieving all Kivra responses");

			var keyValues = kivraClient.getAllResponses();
			if (keyValues == null || keyValues.isEmpty()) {
				LOG.info("No Kivra responses found");
				return emptyList();
			}
			LOG.info("Successfully retrieved {} Kivra responses", keyValues.size());
			return keyValues;
		} catch (Exception e) {
			LOG.error("Exception occurred when retrieving Kivra responses, exception message: {}", e.getMessage(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Exception occurred while retrieving Kivra responses");
		}
	}

	/**
	 * Retrieves a specific registered letter response from Kivra using the provided response key.
	 *
	 * @param  responseKey The key of the response to retrieve.
	 * @return             The RegisteredLetterResponse object containing the details of the response.
	 */
	public RegisteredLetterResponse getRegisteredLetterResponse(final String responseKey) {
		try {
			LOG.info("Retrieving Kivra registered letter response for responseKey: {}", responseKey);
			return kivraClient.getResponseDetails(responseKey);
		} catch (Exception e) {
			LOG.error("Exception occurred when retrieving Kivra registered letter response for responseKey: {}, exception message: {}", responseKey, e.getMessage(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Exception occurred while retrieving Kivra registered letter response for responseKey: " + responseKey);
		}
	}

	/**
	 * Deletes a specific response from Kivra using the provided response key.
	 *
	 * @param responseKey The key of the response to delete.
	 */
	public void deleteResponse(final String responseKey) {
		try {
			LOG.info("Deleting Kivra response with key: {}", responseKey);
			kivraClient.deleteResponse(responseKey);
			LOG.info("Kivra response with key {} deleted successfully", responseKey);
		} catch (Exception e) {
			LOG.error("Exception occurred when deleting Kivra response for responseKey: {}, exception message: {}", responseKey, e.getMessage(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Exception occurred while deleting Kivra response for responseKey: " + responseKey);
		}
	}

	public void healthCheck() {
		kivraClient.getTenantInformation();
	}

}
