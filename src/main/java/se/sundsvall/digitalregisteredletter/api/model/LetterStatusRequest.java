package se.sundsvall.digitalregisteredletter.api.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@Schema(description = "Request model for checking the statuses of given letters")
public record LetterStatusRequest(

	@NotEmpty @ArraySchema(schema = @Schema(implementation = String.class, description = "List of letter IDs to check status for", example = "ac6ffd60-fffe-4fec-bf67-d5f18a62c458")) List<@ValidUuid String> letterIds) {
}
