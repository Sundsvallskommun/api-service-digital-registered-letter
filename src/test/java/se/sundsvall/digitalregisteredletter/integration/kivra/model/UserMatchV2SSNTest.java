package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class UserMatchV2SSNTest {

	private static final String LEGAL_ID = "1234567890";

	@Test
	void testConstructor() {
		var userMatchV2SSN = new UserMatchV2SSN(List.of(LEGAL_ID));

		assertThat(userMatchV2SSN.legalIds()).hasSize(1).containsExactly(LEGAL_ID);
	}

	@Test
	void testBuilder() {
		var userMatchV2SSN = UserMatchV2SSNBuilder.create()
			.withLegalIds(List.of(LEGAL_ID))
			.build();

		assertThat(userMatchV2SSN.legalIds()).hasSize(1).containsExactly(LEGAL_ID);
	}
}
