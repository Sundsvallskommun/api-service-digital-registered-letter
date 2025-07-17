package se.sundsvall.digitalregisteredletter.integration.kivra.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "integration.kivra")
public record KivraProperties(
	@NotBlank String apiUrl,
	@NotNull @Valid OAuth2 oauth2,
	@DefaultValue("PT5S") Duration connectTimeout,
	@DefaultValue("PT15S") Duration readTimeout) {

	public record OAuth2(
		@NotBlank String tokenUrl,
		@NotBlank String clientId,
		@NotBlank String clientSecret,
		@DefaultValue("client_credentials") String authorizationGrantType) {
	}
}
