package se.sundsvall.digitalregisteredletter.integration.party;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.party.PartyType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

@ExtendWith(MockitoExtension.class)
class PartyIntegrationTest {

	@Mock
	private PartyClient partyClient;

	@InjectMocks
	private PartyIntegration partyIntegration;

	@Test
	void getLegalIdByPartyId() {
		var municipalityId = "2281";
		var partyId = "8d9745ee-f5f3-4edf-ad4c-0cda28dd18a6";
		var legalId = "199901011234";

		when(partyClient.getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId)).thenReturn(Optional.of(legalId));

		var result = partyIntegration.getLegalIdByPartyId(municipalityId, partyId);

		assertThat(result).isEqualTo(legalId);
		verify(partyClient).getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId);
	}

	@Test
	void getLegalIdByPartyIdNotFound() {
		var municipalityId = "2281";
		var partyId = "8d9745ee-f5f3-4edf-ad4c-0cda28dd18a6";

		when(partyClient.getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> partyIntegration.getLegalIdByPartyId(municipalityId, partyId))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Request: The given partyId [%s] does not exist in the Party API or is not of type PRIVATE".formatted(partyId));

		verify(partyClient).getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId);
	}

	@Test
	void getLegalIdsByPartyIds() {
		var municipalityId = "2281";
		var partyId1 = "8d9745ee-f5f3-4edf-ad4c-0cda28dd18a6";
		var partyId2 = "1d9745ee-f5f3-4edf-ad4c-0cda28dd18a6";
		var legalId1 = "199901011234";
		var legalId2 = "200001011234";
		when(partyClient.getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId1)).thenReturn(Optional.of(legalId1));
		when(partyClient.getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId2)).thenReturn(Optional.of(legalId2));

		var result = partyIntegration.getLegalIdsByPartyIds(municipalityId, List.of(partyId1, partyId2));

		assertThat(result).containsExactlyInAnyOrder(legalId1, legalId2);

		verify(partyClient).getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId1);
		verify(partyClient).getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId2);
	}

}
