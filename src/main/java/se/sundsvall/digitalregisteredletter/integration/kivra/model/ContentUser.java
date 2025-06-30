package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import java.time.LocalDate;
import se.sundsvall.digitalregisteredletter.support.Builder;

/**
 * Kivra's API specification is not compatible with the openapi-generator plugin, so this class is manually created.
 */
@Builder
public record ContentUser(
	String subject,
	LocalDate generatedAt,
	String type) {
}
