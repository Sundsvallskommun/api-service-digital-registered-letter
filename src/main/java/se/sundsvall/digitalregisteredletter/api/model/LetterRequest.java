package se.sundsvall.digitalregisteredletter.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
@Schema(description = "Request to send a digital registered letter")
public record LetterRequest() {
}
