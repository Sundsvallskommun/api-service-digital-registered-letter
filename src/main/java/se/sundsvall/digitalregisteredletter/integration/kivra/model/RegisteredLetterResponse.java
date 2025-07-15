package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
public record RegisteredLetterResponse(
	String status,
	@JsonProperty("signed_at") OffsetDateTime signedAt,
	@JsonProperty("sender_reference") SenderReference senderReference) {

	public record SenderReference(
		@JsonProperty("sender_internal_id") String internalId) {
	}
}
