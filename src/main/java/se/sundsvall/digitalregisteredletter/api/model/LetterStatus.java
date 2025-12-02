package se.sundsvall.digitalregisteredletter.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
@Schema(description = "Letter status")
public record LetterStatus(

	@Schema(description = "Letter ID", example = "43a32404-28ee-480f-a095-00d48109afab") String letterId,
	@Schema(description = "Delivery status of the letter", example = "SENT") String status,
	@Schema(description = "Information about the signing process", example = "PENDING") String signingInformation) {
}
