package se.sundsvall.digitalregisteredletter.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
public record SupportInfo(

	@NotBlank @Schema(description = "Support text for the letter", examples = "For support, please contact us at the information below.", requiredMode = REQUIRED) String supportText,

	@NotBlank @Schema(description = "URL for contact", examples = "https://example.com/support", requiredMode = REQUIRED) String contactInformationUrl,

	@Schema(description = "Phone number for contact", examples = "+46123456789", requiredMode = REQUIRED) String contactInformationPhoneNumber,

	@Email @NotNull @Schema(description = "Email address for contact", examples = "support@email.com", requiredMode = REQUIRED) String contactInformationEmail) {
}
