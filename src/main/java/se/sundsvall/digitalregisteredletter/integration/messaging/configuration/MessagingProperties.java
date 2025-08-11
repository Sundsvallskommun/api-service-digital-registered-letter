package se.sundsvall.digitalregisteredletter.integration.messaging.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "integration.messaging")
public record MessagingProperties(
	@DefaultValue("5") int connectTimeout,
	@DefaultValue("15") int readTimeout) {
}
