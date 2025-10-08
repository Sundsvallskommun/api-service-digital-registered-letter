package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import se.sundsvall.digitalregisteredletter.support.Builder;

/**
 * Record matching the following response from Kivra:
 * <code>
 * { "status": "signed", "signed_at": "2023-10-01T12:00:00Z", "sender_reference": { "sender_internal_id": "43a32404-28ee-480f-a095-00d48109afab" }, "content_key": "c33e857c-4341-4f54-9044-cdeee56760a8", "bank_id_order": { "orderRef":
 * "be7c5362-7147-47e3-85d6-f358ccec5ca8", "status": "completed", "completionData": { "user": { "personalNumber": "190001011234", "name": "Joe Doe", "givenName": "Joe", "surname": "Doe" }, "device": { "ipAddress": "127.0.0.1" } }, "stepUp": { "mrtd": false
 * }, "signature": "PD94bWwgdmVyc2lvb...", "ocspResponse": "MIIHdgoBAKCCB28wggdrBg..." } }
 * </code>
 */
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
		@JsonProperty("completionData") CompletionData completionData,
		@JsonProperty("stepUp") StepUp stepUp,
		@JsonProperty("signature") String signature,
		@JsonProperty("ocspResponse") String ocspResponse) {

		@Builder
		public record CompletionData(
			@JsonProperty("user") User user,
			@JsonProperty("device") Device device) {

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
		}

		@Builder
		public record StepUp(
			@JsonProperty("mrtd") Boolean mrtd) {
		}
	}
}
