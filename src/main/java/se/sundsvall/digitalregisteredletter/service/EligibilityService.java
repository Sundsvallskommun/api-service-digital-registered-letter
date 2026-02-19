package se.sundsvall.digitalregisteredletter.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import se.sundsvall.digitalregisteredletter.api.model.EligibilityRequest;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.party.PartyIntegration;

import static java.util.Collections.emptyList;

@Service
public class EligibilityService {

	private final KivraIntegration kivraIntegration;
	private final PartyIntegration partyIntegration;

	public EligibilityService(final KivraIntegration kivraIntegration,
		final PartyIntegration partyIntegration) {
		this.kivraIntegration = kivraIntegration;
		this.partyIntegration = partyIntegration;
	}

	public List<String> checkEligibility(final String municipalityId, final String organizationNumber, final EligibilityRequest request) {
		final var partyIdAndLegalIdMap = resolvePartyIdToLegalIdMap(municipalityId, request);
		final var legalIds = partyIdAndLegalIdMap.values().stream().toList();
		if (legalIds.isEmpty()) {
			return emptyList();
		}
		final var eligibleLegalIds = kivraIntegration.checkEligibility(legalIds, municipalityId, organizationNumber);

		return partyIdAndLegalIdMap.entrySet().stream()
			.filter(entry -> eligibleLegalIds.contains(entry.getValue()))
			.map(Map.Entry::getKey)
			.toList();
	}

	private Map<String, String> resolvePartyIdToLegalIdMap(final String municipalityId, final EligibilityRequest request) {
		final var partyIdAndLegalIdMap = new HashMap<String, String>();

		request.partyIds().forEach(party -> {
			final var legalId = partyIntegration.getLegalIdByPartyId(municipalityId, party);
			legalId.ifPresent(value -> partyIdAndLegalIdMap.put(party, value));
		});

		return partyIdAndLegalIdMap;
	}

}
