package se.sundsvall.digitalregisteredletter.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import se.sundsvall.digitalregisteredletter.api.model.EligibilityRequest;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.party.PartyIntegration;

@Service
public class EligibilityService {

	private final KivraIntegration kivraIntegration;
	private final PartyIntegration partyIntegration;

	public EligibilityService(final KivraIntegration kivraIntegration,
		final PartyIntegration partyIntegration) {
		this.kivraIntegration = kivraIntegration;
		this.partyIntegration = partyIntegration;
	}

	public List<String> checkEligibility(final String municipalityId, final EligibilityRequest request) {
		var partyIdAndLegalIdMap = new HashMap<String, String>();

		request.partyIds().forEach(party -> {
			var legalId = partyIntegration.getLegalIdByPartyId(municipalityId, party);
			partyIdAndLegalIdMap.put(party, legalId);
		});

		var legalIds = partyIdAndLegalIdMap.values().stream().toList();

		var eligibleLegalIds = kivraIntegration.checkEligibility(legalIds);

		return partyIdAndLegalIdMap.entrySet().stream()
			.filter(entry -> eligibleLegalIds.contains(entry.getValue()))
			.map(Map.Entry::getKey)
			.toList();
	}

}
