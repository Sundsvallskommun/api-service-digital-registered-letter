package se.sundsvall.digitalregisteredletter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.digitalregisteredletter.api.model.EligibilityRequest;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.party.PartyIntegration;

@ExtendWith(MockitoExtension.class)
class EligibilityServiceTest {

	@Mock
	private KivraIntegration kivraIntegrationMock;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@InjectMocks
	private EligibilityService eligibilityService;

	@Test
	void checkEligibility() {
		var municipalityId = "2281";
		var partyId = "123e4567-e89b-12d3-a456-426614174000";
		var request = new EligibilityRequest(List.of(partyId));
		var legalId = "1234567890";
		var legalIds = List.of(legalId);

		when(partyIntegrationMock.getLegalIdByPartyId(municipalityId, partyId)).thenReturn(Optional.of(legalId));
		when(kivraIntegrationMock.checkEligibility(legalIds)).thenReturn(legalIds);

		var result = eligibilityService.checkEligibility(municipalityId, request);

		assertThat(result).containsExactly(partyId);

		verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, partyId);
		verify(kivraIntegrationMock).checkEligibility(legalIds);
		verifyNoMoreInteractions(partyIntegrationMock, kivraIntegrationMock);

	}

}
