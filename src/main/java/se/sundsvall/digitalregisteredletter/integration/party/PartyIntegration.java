package se.sundsvall.digitalregisteredletter.integration.party;

import static org.zalando.problem.Status.BAD_REQUEST;

import generated.se.sundsvall.party.PartyType;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;

@Component
public class PartyIntegration {

	private final PartyClient partyClient;

	public PartyIntegration(final PartyClient partyClient) {
		this.partyClient = partyClient;
	}

	public String getLegalIdByPartyId(final String municipalityId, final String partyId) {
		return partyClient.getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "The given partyId [%s] does not exist in the Party API or is not of type PRIVATE".formatted(partyId)));
	}

	public List<String> getLegalIdsByPartyIds(final String municipalityId, final List<String> partyIds) {
		return partyIds.stream()
			.map(partyId -> partyClient.getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.toList();
	}

}
