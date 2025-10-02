package se.sundsvall.digitalregisteredletter.api.model;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
@Schema(description = "Digital registered letter response", accessMode = Schema.AccessMode.READ_ONLY)
public record Letter(

	@Schema(description = "Unique identifier for the letter", example = "123e4567-e89b-12d3-a456-426614174000") String id,

	@Schema(description = "Municipality ID for the sender of the letter", example = "2281") String municipalityId,

	@Schema(description = "Status of the letter", examples = {
		"SENT", "SIGNED", "EXPIRED", "FAILED - Client Error", "FAILED - Server Error", "FAILED - Unknown Error"
	}) String status,

	@Schema(description = "The letter body") String body,

	@Schema(description = "Content type of the letter body", example = "text/html") String contentType,

	@Schema(description = "When the letter was sent", example = "2023-10-09T12:34:56+00:00") @DateTimeFormat(iso = DATE_TIME) OffsetDateTime created,

	@Schema(description = "When the letter was last updated", example = "2023-10-09T12:34:56+00:00") @DateTimeFormat(iso = DATE_TIME) OffsetDateTime updated,

	@Schema(description = "Support information for the letter") SupportInfo supportInfo,

	@ArraySchema(schema = @Schema(implementation = Attachment.class, description = "List of attachments for the letter")) List<Attachment> attachments){

	@Builder
	public record Attachment(

		@Schema(description = "Unique identifier for the attachment, used for fetching the attachment content", example = "123e4567-e89b-12d3-a456-426614174001") String id,

		@Schema(description = "Name of the attachment file", example = "document.pdf") String fileName,

		@Schema(description = "Content type of the attachment", example = "application/pdf") String contentType) {
	}

}
