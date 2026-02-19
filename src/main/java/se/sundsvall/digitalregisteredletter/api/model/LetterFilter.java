package se.sundsvall.digitalregisteredletter.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.digitalregisteredletter.support.Builder;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Builder
@Schema(description = "Optional filters to apply when retreiving sent letters")
public record LetterFilter(
	@Schema(description = "Optional filter for matching organization id", examples = "44", requiredMode = NOT_REQUIRED) Integer orgId,

	@Schema(description = "Optional filter for matching username", examples = "joe01doe", requiredMode = NOT_REQUIRED) String username,

	@Schema(description = "Optional filter with format YYYY-MM-DD for matching earliest date when letter was sent", examples = "2025-08-18", requiredMode = NOT_REQUIRED) @DateTimeFormat(iso = DATE) OffsetDateTime createdEarliest,

	@Schema(description = "Optional filter with format YYYY-MM-DD for matching latest date when letter was sent", examples = "2025-09-18", requiredMode = NOT_REQUIRED) @DateTimeFormat(iso = DATE) OffsetDateTime createdLatest) {
}
