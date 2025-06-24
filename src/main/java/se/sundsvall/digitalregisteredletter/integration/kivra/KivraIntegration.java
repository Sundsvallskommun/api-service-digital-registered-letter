package se.sundsvall.digitalregisteredletter.integration.kivra;

import static java.util.Collections.emptyList;
import static org.zalando.problem.Status.BAD_GATEWAY;
import static se.sundsvall.digitalregisteredletter.integration.kivra.KivraMapper.toCheckEligibilityRequest;
import static se.sundsvall.digitalregisteredletter.integration.kivra.KivraMapper.toSendContentRequest;

import generated.com.kivra.ContentUser;
import generated.com.kivra.UserMatchV2SSN;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.integration.db.LetterEntity;

@Component
public class KivraIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(KivraIntegration.class);
	private final KivraClient kivraClient;

	public KivraIntegration(final KivraClient kivraClient) {
		this.kivraClient = kivraClient;
	}

	/**
	 * Checks if the provided legal IDs are eligible for Kivra.
	 *
	 * @param  legalIds List of legal IDs to check eligibility for.
	 * @return          List of legal IDs that are eligible for Kivra.
	 */
	public List<String> checkEligibility(final List<String> legalIds) {
		try {
			var request = toCheckEligibilityRequest(legalIds);
			LOG.info("Checking Kivra eligibility for legal ids: {}", legalIds);
			var response = kivraClient.checkEligibility(request);
			LOG.info("Kivra eligibility check successful");
			return Optional.ofNullable(response.getBody()).map(UserMatchV2SSN::getList).orElse(emptyList());
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
	 * @return              ContentUser object containing the response from Kivra.
	 */
	public ContentUser sendContent(final LetterEntity letterEntity, final String legalId) {
		try {
			var request = toSendContentRequest(letterEntity, legalId);
			LOG.info("Sending content to Kivra for legal id: {}", legalId);
			var response = kivraClient.sendContent(request);
			LOG.info("Kivra content sent successfully for legal id: {}", legalId);
			return response.getBody();
		} catch (Exception e) {
			LOG.error("Exception occurred when sending content to Kivra for legal id: {}, exception message: {}", legalId, e.getMessage(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Exception occurred while sending content to Kivra for legal id: " + legalId);
		}
	}

}
