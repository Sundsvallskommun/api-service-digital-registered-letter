package se.sundsvall.digitalregisteredletter.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
@Schema(description = "Digital registered letter response")
public record LetterResponse() {
}
