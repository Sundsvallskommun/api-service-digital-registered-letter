package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyValueTest {

	private static final String RESPONSE_KEY = "responseKey";
	private static final String STATUS = "status";

	@Test
	void constructorTest() {
		final var keyValue = new KeyValue(RESPONSE_KEY, STATUS);

		assertThat(keyValue).isNotNull();
		assertThat(keyValue.responseKey()).isEqualTo(RESPONSE_KEY);
		assertThat(keyValue.status()).isEqualTo(STATUS);
	}

	@Test
	void builderTest() {
		final var keyValue = KeyValueBuilder.create()
			.withResponseKey(RESPONSE_KEY)
			.withStatus(STATUS)
			.build();

		assertThat(keyValue).isNotNull();
		assertThat(keyValue.responseKey()).isEqualTo(RESPONSE_KEY);
		assertThat(keyValue.status()).isEqualTo(STATUS);
	}

}
