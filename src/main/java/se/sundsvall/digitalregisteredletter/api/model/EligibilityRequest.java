package se.sundsvall.digitalregisteredletter.api.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@Schema(description = "Eligibility request model")
public record EligibilityRequest(

	@ArraySchema(schema = @Schema(implementation = String.class, description = "List of party IDs to check for eligibility", examples = "123e4567-e89b-12d3-a456-426614174000")) List<@ValidUuid String> partyIds) {
}
