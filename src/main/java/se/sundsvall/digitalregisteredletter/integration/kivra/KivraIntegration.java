package se.sundsvall.digitalregisteredletter.integration.kivra;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.zalando.problem.Status.BAD_GATEWAY;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.digitalregisteredletter.Constants.STATUS_CLIENT_ERROR;
import static se.sundsvall.digitalregisteredletter.Constants.STATUS_EXCEPTION_ERROR;
import static se.sundsvall.digitalregisteredletter.Constants.STATUS_SENT;
import static se.sundsvall.digitalregisteredletter.Constants.STATUS_SERVER_ERROR;
import static se.sundsvall.digitalregisteredletter.Constants.STATUS_UNKNOWN_ERROR;

import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.exception.ServerProblem;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.configuration.KivraProperties;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.KeyValue;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponse;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.UserMatchV2SSN;
import se.sundsvall.digitalregisteredletter.service.TenantService;

@Component
public class KivraIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(KivraIntegration.class);
	private final KivraClient kivraClient;
	private final KivraMapper kivraMapper;
	private final KivraProperties kivraProperties;
	private final TenantService tenantService;

	public KivraIntegration(final KivraClient kivraClient,
		final KivraMapper kivraMapper,
		final KivraProperties kivraProperties,
		final TenantService tenantService) {
		this.kivraClient = kivraClient;
		this.kivraMapper = kivraMapper;
		this.kivraProperties = kivraProperties;
		this.tenantService = tenantService;
	}

	private URI buildTenantUri(final String tenantKey) {
		return URI.create(kivraProperties.baseUrl() + "/" + tenantKey);
	}

	private URI buildLegacyUri() {
		return URI.create(kivraProperties.apiUrl());
	}

	/**
	 * Checks if the provided legal IDs are eligible for Kivra using a dynamically resolved tenant key.
	 *
	 * @param  legalIds           List of legal IDs to check eligibility for.
	 * @param  municipalityId     The municipality ID used to look up the tenant.
	 * @param  organizationNumber The organization number used to look up the tenant.
	 * @return                    List of legal IDs that are eligible for Kivra.
	 */
	public List<String> checkEligibility(final List<String> legalIds, final String municipalityId, final String organizationNumber) {
		final var tenantKey = tenantService.getDecryptedTenantKey(municipalityId, organizationNumber);
		try {
			final var request = kivraMapper.toCheckEligibilityRequest(legalIds);
			LOG.info("Checking Kivra eligibility for legal ids: {}", legalIds);
			final var response = kivraClient.checkEligibility(buildTenantUri(tenantKey), request);
			LOG.info("Kivra eligibility check successful");
			return ofNullable(response).map(UserMatchV2SSN::legalIds).orElse(emptyList());

		} catch (final ServerProblem e) {
			LOG.error("Server exception occurred when checking Kivra eligibility for legal ids: {}, exception message: {}", legalIds, e.getMessage(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Server exception occurred while checking Kivra eligibility for legal ids: " + legalIds);
		} catch (final Exception e) {
			LOG.error("Exception occurred when checking Kivra eligibility for legal ids: {}, exception message: {}", legalIds, e.getMessage(), e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Exception occurred while checking Kivra eligibility for legal ids: " + legalIds);
		}
	}

	/**
	 * @deprecated Use {@link #checkEligibility(List, String, String)} with municipalityId and organizationNumber instead.
	 */
	@Deprecated(forRemoval = true)
	public List<String> checkEligibility(final List<String> legalIds) {
		try {
			final var request = kivraMapper.toCheckEligibilityRequest(legalIds);
			LOG.info("Checking Kivra eligibility for legal ids: {}", legalIds);
			final var response = kivraClient.checkEligibility(buildLegacyUri(), request);
			LOG.info("Kivra eligibility check successful");
			return ofNullable(response).map(UserMatchV2SSN::legalIds).orElse(emptyList());

		} catch (final ServerProblem e) {
			LOG.error("Server exception occurred when checking Kivra eligibility for legal ids: {}, exception message: {}", legalIds, e.getMessage(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Server exception occurred while checking Kivra eligibility for legal ids: " + legalIds);
		} catch (final Exception e) {
			LOG.error("Exception occurred when checking Kivra eligibility for legal ids: {}, exception message: {}", legalIds, e.getMessage(), e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Exception occurred while checking Kivra eligibility for legal ids: " + legalIds);
		}
	}

	/**
	 * Sends content to Kivra for the specified letter entity and legal ID using a dynamically resolved tenant key.
	 *
	 * @param  letterEntity       The letter entity containing the content to be sent.
	 * @param  legalId            The legal ID of the recipient.
	 * @param  municipalityId     The municipality ID used to look up the tenant.
	 * @param  organizationNumber The organization number used to look up the tenant.
	 * @return                    The status that indicates whether the content was sent successfully or if there was an
	 *                            error.
	 */
	public String sendContent(final LetterEntity letterEntity, final String legalId, final String municipalityId, final String organizationNumber) {
		final var tenantKey = tenantService.getDecryptedTenantKey(municipalityId, organizationNumber);
		try {
			final var request = kivraMapper.toSendContentRequest(letterEntity, legalId);
			LOG.info("Sending content to Kivra for legal id: {}", legalId);
			kivraClient.sendContent(buildTenantUri(tenantKey), request);
			LOG.info("Content sent successfully for legal id: {}", legalId);

			return STATUS_SENT;

		} catch (final ClientProblem e) {
			LOG.error("Response indicates client error", e);
			return STATUS_CLIENT_ERROR;
		} catch (final ServerProblem e) {
			LOG.error("Response indicates server error", e);
			return STATUS_SERVER_ERROR;
		} catch (final ThrowableProblem e) {
			LOG.error("Response indicates unknown error", e);
			return STATUS_UNKNOWN_ERROR;
		} catch (final Exception e) {
			LOG.error("{} occurred when sending content to Kivra for legal id: {}, exception message: {}", e.getClass().getSimpleName(), legalId, e.getMessage(), e);
			return STATUS_EXCEPTION_ERROR;
		}
	}

	/**
	 * @deprecated Use {@link #sendContent(LetterEntity, String, String, String)} with municipalityId and organizationNumber
	 *             instead.
	 */
	@Deprecated(forRemoval = true)
	public String sendContent(final LetterEntity letterEntity, final String legalId) {
		try {
			final var request = kivraMapper.toSendContentRequest(letterEntity, legalId);
			LOG.info("Sending content to Kivra for legal id: {}", legalId);
			kivraClient.sendContent(buildLegacyUri(), request);
			LOG.info("Content sent successfully for legal id: {}", legalId);

			return STATUS_SENT;

		} catch (final ClientProblem e) {
			LOG.error("Response indicates client error", e);
			return STATUS_CLIENT_ERROR;
		} catch (final ServerProblem e) {
			LOG.error("Response indicates server error", e);
			return STATUS_SERVER_ERROR;
		} catch (final ThrowableProblem e) {
			LOG.error("Response indicates unknown error", e);
			return STATUS_UNKNOWN_ERROR;
		} catch (final Exception e) {
			LOG.error("{} occurred when sending content to Kivra for legal id: {}, exception message: {}", e.getClass().getSimpleName(), legalId, e.getMessage(), e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Exception occurred while sending content to Kivra for legal id: " + legalId);
		}
	}

	public List<KeyValue> getAllResponses(final String municipalityId, final String organizationNumber) {
		final var tenantKey = tenantService.getDecryptedTenantKey(municipalityId, organizationNumber);
		try {
			LOG.info("Retrieving all Kivra responses for organizationNumber: {}", organizationNumber);

			final var keyValues = ofNullable(kivraClient.getAllResponses(buildTenantUri(tenantKey))).orElse(emptyList());
			if (isEmpty(keyValues)) {
				LOG.info("No Kivra responses found for organizationNumber: {}", organizationNumber);
			} else {
				LOG.info("Successfully retrieved {} Kivra responses for organizationNumber: {}", keyValues.size(), organizationNumber);
			}

			return keyValues;

		} catch (final ServerProblem e) {
			LOG.error("Server exception occurred when retrieving Kivra responses for organizationNumber: {}, exception message: {}", organizationNumber, e.getMessage(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Server exception occurred while retrieving Kivra responses");
		} catch (final Exception e) {
			LOG.error("Exception occurred when retrieving Kivra responses for organizationNumber: {}, exception message: {}", organizationNumber, e.getMessage(), e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Exception occurred while retrieving Kivra responses");
		}
	}

	public RegisteredLetterResponse getRegisteredLetterResponse(final String responseKey, final String municipalityId, final String organizationNumber) {
		final var tenantKey = tenantService.getDecryptedTenantKey(municipalityId, organizationNumber);
		try {
			LOG.info("Retrieving Kivra registered letter response for responseKey: {}", responseKey);
			return kivraClient.getResponseDetails(buildTenantUri(tenantKey), responseKey);

		} catch (final ServerProblem e) {
			LOG.error("Server exception occurred when retrieving Kivra registered letter response for responseKey: {}, exception message: {}", responseKey, e.getMessage(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Exception occurred while retrieving Kivra registered letter response for responseKey: " + responseKey);
		} catch (final Exception e) {
			LOG.error("Exception occurred when retrieving Kivra registered letter response for responseKey: {}, exception message: {}", responseKey, e.getMessage(), e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Exception occurred while retrieving Kivra registered letter response for responseKey: " + responseKey);
		}
	}

	public void deleteResponse(final String responseKey, final String municipalityId, final String organizationNumber) {
		final var tenantKey = tenantService.getDecryptedTenantKey(municipalityId, organizationNumber);
		try {
			LOG.info("Deleting Kivra response with key: {}", responseKey);
			kivraClient.deleteResponse(buildTenantUri(tenantKey), responseKey);
			LOG.info("Kivra response with key {} deleted successfully", responseKey);

		} catch (final ServerProblem e) {
			LOG.error("Server exception occurred when deleting Kivra response for responseKey: {}, exception message: {}", responseKey, e.getMessage(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Server exception occurred while deleting Kivra response for responseKey: " + responseKey);
		} catch (final Exception e) {
			LOG.error("Exception occurred when deleting Kivra response for responseKey: {}, exception message: {}", responseKey, e.getMessage(), e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Exception occurred while deleting Kivra response for responseKey: " + responseKey);
		}
	}

	public void healthCheck() {
		kivraClient.getTenantInformation(buildLegacyUri());
	}

}
