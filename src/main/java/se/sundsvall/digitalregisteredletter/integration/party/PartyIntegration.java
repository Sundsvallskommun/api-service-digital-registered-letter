package se.sundsvall.digitalregisteredletter.integration.party;

import generated.se.sundsvall.party.PartyType;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PartyIntegration {

	private final PartyClient partyClient;

	public PartyIntegration(final PartyClient partyClient) {
		this.partyClient = partyClient;
	}

	public Optional<String> getLegalIdByPartyId(final String municipalityId, final String partyId) {
		return partyClient.getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId);
	}

	public List<String> getLegalIdsByPartyIds(final String municipalityId, final List<String> partyIds) {
		return partyIds.stream()
			.map(partyId -> partyClient.getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.toList();
	}

}
