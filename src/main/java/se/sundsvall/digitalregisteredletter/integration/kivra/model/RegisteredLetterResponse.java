package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
public record RegisteredLetterResponse(
	@JsonProperty("status") String status,
	@JsonProperty("signed_at") OffsetDateTime signedAt,
	@JsonProperty("sender_reference") SenderReference senderReference,
	@JsonProperty("content_key") String contentKey,
	@JsonProperty("bank_id_order") BankIdOrder bankIdOrder) {

	@Builder
	public record SenderReference(
		@JsonProperty("sender_internal_id") String internalId) {
	}

	@Builder
	public record BankIdOrder(
		@JsonProperty("orderRef") String orderRef,
		@JsonProperty("status") String status,
		@JsonProperty("completionData") CompletionData completionData) {

		@Builder
		public record CompletionData(
			@JsonProperty("user") User user,
			@JsonProperty("device") Device device,
			@JsonProperty("stepUp") StepUp stepUp,
			@JsonProperty("signature") String signature,
			@JsonProperty("ocspResponse") String ocspResponse) {

			@Builder
			public record User(
				@JsonProperty("personalNumber") String personalNumber,
				@JsonProperty("name") String name,
				@JsonProperty("givenName") String givenName,
				@JsonProperty("surname") String surname) {
			}

			@Builder
			public record Device(
				@JsonProperty("ipAddress") String ipAddress) {
			}

			@Builder
			public record StepUp(
				@JsonProperty("mrtd") Boolean mrtd) {
			}
		}

	}
}
