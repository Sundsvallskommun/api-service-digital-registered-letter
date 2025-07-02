package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
public record KeyValue(
	@JsonProperty("key") String responseKey,
	String status) {
}
