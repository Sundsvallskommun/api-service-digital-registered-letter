package se.sundsvall.digitalregisteredletter.integration.kivra.configuration;

import feign.Request;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.decoder.JsonPathErrorDecoder;
import se.sundsvall.dept44.configuration.feign.interceptor.OAuth2RequestInterceptor;
import se.sundsvall.dept44.configuration.feign.retryer.ActionRetryer;

import static java.util.Collections.emptySet;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Import(FeignConfiguration.class)
@EnableConfigurationProperties(KivraProperties.class)
public class KivraConfiguration {

	public static final String CLIENT_ID = "kivra";

	private final KivraProperties properties;

	public KivraConfiguration(final KivraProperties properties) {
		this.properties = properties;
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer() {
		return builder -> {
			final var clientRegistration = ClientRegistration.withRegistrationId(CLIENT_ID)
				.tokenUri(properties.oauth2().tokenUrl())
				.clientId(properties.oauth2().clientId())
				.clientSecret(properties.oauth2().clientSecret())
				.authorizationGrantType(new AuthorizationGrantType(properties.oauth2().authorizationGrantType()))
				.build();
			final var oAuth2RequestInterceptor = new OAuth2RequestInterceptor(clientRegistration, emptySet());

			builder.requestInterceptor(oAuth2RequestInterceptor)
				.retryer(new ActionRetryer(oAuth2RequestInterceptor::removeToken, 1))
				.errorDecoder(new JsonPathErrorDecoder(CLIENT_ID, new JsonPathErrorDecoder.JsonPathSetup("$.long_message")))
				.options(feignOptions());
		};
	}

	private Request.Options feignOptions() {
		return new Request.Options(
			properties.connectTimeout().toMillis(), MILLISECONDS,
			properties.readTimeout().toMillis(), MILLISECONDS,
			true);
	}
}
