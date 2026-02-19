package se.sundsvall.digitalregisteredletter.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import se.sundsvall.digitalregisteredletter.support.Builder;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

@Builder
@Schema(description = "Tenant model")
public record Tenant(

	@Schema(description = "Unique identifier for the tenant", examples = "cb20c51f-fcf3-42c0-b613-de563634a8ec", accessMode = READ_ONLY) String id,

	@NotBlank @Schema(description = "Organization number", examples = "5591628136") String orgNumber,

	@NotBlank @Schema(description = "Tenant key identifier", examples = "some-tenant-key", accessMode = WRITE_ONLY) String tenantKey) {
}
