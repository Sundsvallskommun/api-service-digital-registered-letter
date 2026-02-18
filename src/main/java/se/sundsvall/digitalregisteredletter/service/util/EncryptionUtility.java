package se.sundsvall.digitalregisteredletter.service.util;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.configuration.CredentialsProperties;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

@Component
public class EncryptionUtility {

	private static final String ENCRYPT_ALGO = "ChaCha20-Poly1305/None/NoPadding";

	private static final int NONCE_LEN = 12; // bytes

	private final CredentialsProperties credentialsProperties;
	private final SecureRandom random;

	public EncryptionUtility(final CredentialsProperties credentialsProperties) {
		this.credentialsProperties = credentialsProperties;
		random = new SecureRandom();
	}

	private SecretKeySpec getSecretKeySpec() {
		return new SecretKeySpec(credentialsProperties.secretKey().getBytes(), "ChaCha20-Poly1305");
	}

	public String encrypt(final byte[] input) {

		final var key = getSecretKeySpec();

		final var nonce = new byte[NONCE_LEN];
		random.nextBytes(nonce);

		final var ivParameterSpec = new IvParameterSpec(nonce);
		final byte[] messageCipher;
		final Cipher cipher;
		try {
			cipher = Cipher.getInstance(ENCRYPT_ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
			messageCipher = cipher.doFinal(input);
		} catch (final GeneralSecurityException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Something went wrong encrypting input");
		}

		final var cipherText = new byte[messageCipher.length + NONCE_LEN];
		System.arraycopy(nonce, 0, cipherText, 0, NONCE_LEN);
		System.arraycopy(messageCipher, 0, cipherText, NONCE_LEN,
			messageCipher.length);

		return Base64.getEncoder().encodeToString(cipherText);
	}

	public String decrypt(final String base64Input) {

		final var input = Base64.getDecoder().decode(base64Input);

		final var key = getSecretKeySpec();

		final var nonce = new byte[NONCE_LEN];
		System.arraycopy(input, 0, nonce, 0, NONCE_LEN);

		final var messageCipher = new byte[input.length - NONCE_LEN];
		System.arraycopy(input, NONCE_LEN, messageCipher, 0, input.length - NONCE_LEN);

		final var ivParameterSpec = new IvParameterSpec(nonce);

		final Cipher cipher;

		try {
			cipher = Cipher.getInstance(ENCRYPT_ALGO);
			cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
			return new String(cipher.doFinal(messageCipher));
		} catch (final GeneralSecurityException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Something went wrong decrypting input");
		}

	}

}
