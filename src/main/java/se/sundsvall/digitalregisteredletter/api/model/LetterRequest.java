package se.sundsvall.digitalregisteredletter.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.sundsvall.dept44.common.validators.annotation.OneOf;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
@Schema(description = "Request to send a digital registered letter")
public record LetterRequest(

	@ValidUuid @Schema(description = "Party ID of the recipient", example = "7ca29702-a07f-4e13-a66a-4ebc27929cfd", requiredMode = REQUIRED) String partyId,

	@NotBlank @Schema(description = "Subject of the letter", example = "Important Notification", requiredMode = REQUIRED) String subject,

	@Valid @NotNull @Schema(implementation = SupportInfo.class, description = "Support information for the letter") SupportInfo supportInfo,

	@OneOf({
		TEXT_PLAIN_VALUE, TEXT_HTML_VALUE
	}) @Schema(description = "Content type of the letter body, e.g., 'text/plain' or 'text/html'", example = "text/plain", requiredMode = REQUIRED) String contentType,

	@NotBlank @Schema(description = "Body of the letter", example = "This is the content of the letter. Plain-text body", requiredMode = REQUIRED) String body) {

}
