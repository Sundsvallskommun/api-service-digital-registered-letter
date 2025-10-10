package se.sundsvall.digitalregisteredletter.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Letter status")
public record LetterStatus(

	@Schema(description = "Letter ID", example = "43a32404-28ee-480f-a095-00d48109afab") String letterId,
	@Schema(description = "Current status of the letter. NOT_FOUND if the entity could not be found", example = "NEW") String status) {}
