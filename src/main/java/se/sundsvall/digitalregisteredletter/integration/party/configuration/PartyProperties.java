package se.sundsvall.digitalregisteredletter.integration.party.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "integration.party")
public record PartyProperties(
	@DefaultValue("5") int connectTimeout,
	@DefaultValue("15") int readTimeout) {
}
