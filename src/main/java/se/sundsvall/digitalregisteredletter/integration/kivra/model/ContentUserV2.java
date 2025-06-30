package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.sundsvall.digitalregisteredletter.support.Builder;

/**
 * Kivra's API specification is not compatible with the openapi-generator plugin, so this class is manually created.
 */
@Builder
public record ContentUserV2(
	@JsonProperty("ssn") String legalId,
	String subject,
	String type,
	RegisteredLetter registered,
	List<PartsResponsive> parts) {

	@Builder
	public record PartsResponsive(
		String name,
		String data,
		String contentType) {
	}

	@Builder
	public record RegisteredLetter(
		String expiresAt,
		String senderReference,
		RegisteredLetterHidden hidden) {

		@Builder
		public record RegisteredLetterHidden(
			Boolean sender,
			Boolean subject) {
		}
	}
}
