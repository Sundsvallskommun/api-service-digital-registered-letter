package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.sundsvall.digitalregisteredletter.support.Builder;

/**
 * Kivra's API specification is not compatible with the openapi-generator plugin, so this class is manually created.
 */
@Builder
public record UserMatchV2SSN(
	@JsonProperty("list") List<String> legalIds) {
}
