package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
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
		@JsonProperty("content_type") String contentType) {
	}

	@Builder
	public record RegisteredLetter(
		@JsonProperty("expires_at") OffsetDateTime expiresAt,
		@JsonProperty("sender_reference") SenderReference senderReference,
		RegisteredLetterHidden hidden) {

		public record SenderReference(
			@JsonProperty("sender_internal_id") String senderInternalId) {
		}

		@Builder
		public record RegisteredLetterHidden(
			Boolean sender,
			Boolean subject) {
		}
	}
}
