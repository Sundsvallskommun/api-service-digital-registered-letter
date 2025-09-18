package se.sundsvall.digitalregisteredletter.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
@Schema(description = "Optional filters to apply when retreiving sent letters")
public record LetterFilter(
	@Schema(description = "Optional filter on organization id", example = "44", requiredMode = NOT_REQUIRED) Integer orgId,

	@Schema(description = "Optional filter on username", example = "joe01doe", requiredMode = NOT_REQUIRED) String username,

	@Schema(description = "Optional filter on earliest date when letter was sent", example = "2025-08-18", requiredMode = NOT_REQUIRED) @DateTimeFormat(iso = DATE_TIME) OffsetDateTime createdEarliest,

	@Schema(description = "Optional filter on latest date when letter was sent", example = "2025-09-18", requiredMode = NOT_REQUIRED) @DateTimeFormat(iso = DATE_TIME) OffsetDateTime createdLatest) {
}
