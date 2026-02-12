package se.sundsvall.digitalregisteredletter.service.util;

import static java.util.Base64.getEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import se.sundsvall.digitalregisteredletter.configuration.CredentialsProperties;

class EncryptionUtilityTest {

	// ChaCha20-Poly1305 requires a 256-bit (32-byte) key
	private static final String SECRET_KEY = "01234567890123456789012345678901";
	private static final CredentialsProperties CREDENTIALS = new CredentialsProperties(SECRET_KEY);

	private final EncryptionUtility encryptionUtility = new EncryptionUtility(CREDENTIALS);

	@Test
	void encryptAndDecryptRoundTrip() {
		final var plaintext = "some-tenant-key-value";

		final var encrypted = encryptionUtility.encrypt(plaintext.getBytes());
		final var decrypted = encryptionUtility.decrypt(encrypted);

		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	void encryptProducesDifferentCiphertextEachTime() {
		final var plaintext = "same-input";

		final var encrypted1 = encryptionUtility.encrypt(plaintext.getBytes());
		final var encrypted2 = encryptionUtility.encrypt(plaintext.getBytes());

		assertThat(encrypted1).isNotEqualTo(encrypted2);
	}

	@Test
	void encryptedOutputDiffersFromPlaintext() {
		final var plaintext = "sensitive-data";

		final var encrypted = encryptionUtility.encrypt(plaintext.getBytes());

		assertThat(encrypted).isNotEqualTo(plaintext);
	}

	@Test
	void decryptWithInvalidInputThrowsException() {
		// Valid base64 but garbage cipher data (needs to be at least 12 bytes for nonce extraction)
		final var invalidCipherText = getEncoder().encodeToString(new byte[32]);

		assertThatThrownBy(() -> encryptionUtility.decrypt(invalidCipherText))
			.isInstanceOf(EncryptionException.class)
			.hasMessageContaining("Something went wrong decrypting input");
	}

	@Test
	void encryptWithInvalidKeyThrowsException() {
		final var badCredentials = new CredentialsProperties("short-key");
		final var badUtility = new EncryptionUtility(badCredentials);

		assertThatThrownBy(() -> badUtility.encrypt("test".getBytes()))
			.isInstanceOf(EncryptionException.class)
			.hasMessageContaining("Something went wrong encrypting input");
	}
}
