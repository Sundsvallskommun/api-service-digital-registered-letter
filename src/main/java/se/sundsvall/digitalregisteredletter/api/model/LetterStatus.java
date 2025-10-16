package se.sundsvall.digitalregisteredletter.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
@Schema(description = "Letter status")
public record LetterStatus(

	@Schema(description = "Letter ID", example = "43a32404-28ee-480f-a095-00d48109afab") String letterId,
	@Schema(description = "Delivery status of the letter. NOT_FOUND if the letter could not be found", example = "NEW") String status,
	@Schema(description = "Information about the signing process. NOT_FOUND if there is no signing information available", example = "COMPLETED") String signingInformation) {}
