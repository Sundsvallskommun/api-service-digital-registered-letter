package se.sundsvall.digitalregisteredletter.integration.kivra.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "integration.kivra")
public record KivraProperties(
	@DefaultValue("5") int connectTimeout,
	@DefaultValue("15") int readTimeout) {
}
