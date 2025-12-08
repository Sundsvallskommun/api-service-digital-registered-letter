package se.sundsvall.digitalregisteredletter.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
public record Organization(

	@NotNull @Schema(description = "Unique id for the organization", examples = "44", requiredMode = REQUIRED) Integer number,

	@NotBlank @Schema(description = "Readable name for the organization", examples = "Department 44", requiredMode = REQUIRED) String name) {
}
