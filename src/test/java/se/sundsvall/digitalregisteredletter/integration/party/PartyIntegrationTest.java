package se.sundsvall.digitalregisteredletter.integration.party;

import generated.se.sundsvall.party.PartyType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartyIntegrationTest {

	@Mock
	private PartyClient partyClient;

	@InjectMocks
	private PartyIntegration partyIntegration;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(partyClient);
	}

	@Test
	void getLegalIdByPartyId() {
		var municipalityId = "2281";
		var partyId = "8d9745ee-f5f3-4edf-ad4c-0cda28dd18a6";
		var optionalLegalId = Optional.of("199901011234");

		when(partyClient.getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId)).thenReturn(optionalLegalId);

		var result = partyIntegration.getLegalIdByPartyId(municipalityId, partyId);

		assertThat(result).isEqualTo(optionalLegalId);
		verify(partyClient).getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId);
	}

	@Test
	void getLegalIdByPartyIdNotFound() {
		var municipalityId = "2281";
		var partyId = "8d9745ee-f5f3-4edf-ad4c-0cda28dd18a6";

		when(partyClient.getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId)).thenReturn(Optional.empty());

		var result = partyIntegration.getLegalIdByPartyId(municipalityId, partyId);

		assertThat(result).isEmpty();
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
