package se.sundsvall.digitalregisteredletter.integration.kivra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.integration.db.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUser;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserV2;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.UserMatchV2SSN;

@ExtendWith(MockitoExtension.class)
class KivraIntegrationTest {

	@Mock
	private KivraClient kivraClient;

	@InjectMocks
	private KivraIntegration kivraIntegration;

	@Test
	void checkEligibilityTest() {
		var legalId = "1234567890";
		var response = ResponseEntity.ok(new UserMatchV2SSN(List.of(legalId)));
		var requestCaptor = ArgumentCaptor.forClass(UserMatchV2SSN.class);

		when(kivraClient.checkEligibility(requestCaptor.capture())).thenReturn(response);

		var result = kivraIntegration.checkEligibility(List.of(legalId));
		assertThat(result).isNotNull().hasSize(1).containsExactly(legalId);

		var capturedRequest = requestCaptor.getValue();
		assertThat(capturedRequest).isNotNull();
		assertThat(capturedRequest.legalIds()).hasSize(1).containsExactly(legalId);

		verify(kivraClient).checkEligibility(capturedRequest);
	}

	@Test
	void checkEligibilityKivraThrows() {
		var legalId = "1234567890";
		var legalIds = List.of(legalId);
		when(kivraClient.checkEligibility(new UserMatchV2SSN(legalIds))).thenThrow(new RuntimeException("Kivra service error"));

		assertThatThrownBy(() -> kivraIntegration.checkEligibility(legalIds))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Exception occurred while checking Kivra eligibility for legal ids: %s", legalIds);

		verify(kivraClient).checkEligibility(new UserMatchV2SSN(legalIds));
	}

	@Test
	void sendContentTest() {
		var letterEntity = new LetterEntity();
		var legalId = "1234567890";
		var response = ResponseEntity.ok(ContentUserBuilder.create()
			.withSubject("subject")
			.withGeneratedAt(LocalDate.MIN)
			.withType("registered.letter")
			.build());
		var requestCaptor = ArgumentCaptor.forClass(ContentUserV2.class);

		when(kivraClient.sendContent(requestCaptor.capture())).thenReturn(response);

		var result = kivraIntegration.sendContent(letterEntity, legalId);

		assertThat(result).isNotNull().isInstanceOf(ContentUser.class);
		assertThat(result.subject()).isEqualTo("subject");
		assertThat(result.generatedAt()).isEqualTo(LocalDate.MIN);
		assertThat(result.type()).isEqualTo("registered.letter");

		var capturedRequest = requestCaptor.getValue();
		assertThat(capturedRequest).isNotNull().isInstanceOf(ContentUserV2.class);
		assertThat(capturedRequest.subject()).isEqualTo(letterEntity.getSubject());
		assertThat(capturedRequest.legalId()).isEqualTo(legalId);
		assertThat(capturedRequest.type()).isEqualTo("registered.letter");

		verify(kivraClient).sendContent(capturedRequest);
	}

	@Test
	void sendContentKivraThrows() {
		var letterEntity = new LetterEntity();
		var legalId = "1234567890";
		when(kivraClient.sendContent(any())).thenThrow(new RuntimeException("Kivra service error"));

		assertThatThrownBy(() -> kivraIntegration.sendContent(letterEntity, legalId))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Gateway: Exception occurred while sending content to Kivra for legal id: %s", legalId);

		verify(kivraClient).sendContent(any());
	}

}
