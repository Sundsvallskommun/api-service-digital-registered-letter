package se.sundsvall.digitalregisteredletter.integration.postportalservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "integration.postportalservice")
public record PostportalserviceProperties(
	@DefaultValue("5") int connectTimeout,
	@DefaultValue("15") int readTimeout) {
}
