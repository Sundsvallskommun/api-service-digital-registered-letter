package se.sundsvall.digitalregisteredletter.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import se.sundsvall.dept44.common.validators.annotation.ValidOrganizationNumber;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
@Schema(description = "Request for creating a tenant in Kivra")
public record KivraTenantRequest(
	@NotBlank @Schema(description = "Name of the tenant organization", examples = "Department 44") String name,
	@Schema(description = "Legal name of the tenant organization", examples = "Department 44 ab") String legalName,
	@NotBlank @ValidOrganizationNumber @Schema(description = "Organisation number of the tenant organization", examples = "5561234567") String orgNumber) {

}
