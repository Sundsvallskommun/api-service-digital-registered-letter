package se.sundsvall.digitalregisteredletter.service.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EncryptionExceptionTest {

	@Test
	void constructorWithMessageAndCause() {
		final var message = "encryption failed";
		final var cause = new RuntimeException("root cause");

		final var exception = new EncryptionException(message, cause);

		assertThat(exception.getMessage()).isEqualTo(message);
		assertThat(exception.getCause()).isSameAs(cause);
	}

	@Test
	void constructorWithMessageOnly() {
		final var message = "encryption failed";

		final var exception = new EncryptionException(message);

		assertThat(exception.getMessage()).isEqualTo(message);
		assertThat(exception.getCause()).isNull();
	}
}
