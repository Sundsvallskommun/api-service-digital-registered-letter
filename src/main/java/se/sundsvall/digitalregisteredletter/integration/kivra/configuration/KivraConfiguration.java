package se.sundsvall.digitalregisteredletter.integration.kivra.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.JsonPathErrorDecoder;

@Import(FeignConfiguration.class)
@EnableConfigurationProperties(KivraProperties.class)
public class KivraConfiguration {

	public static final String CLIENT_ID = "kivra";

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final KivraProperties kivraProperties, final ClientRegistrationRepository clientRegistrationRepository) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new JsonPathErrorDecoder(CLIENT_ID, new JsonPathErrorDecoder.JsonPathSetup("$.long_message")))
			.withRequestTimeoutsInSeconds(kivraProperties.connectTimeout(), kivraProperties.readTimeout())
			.withRetryableOAuth2InterceptorForClientRegistration(clientRegistrationRepository.findByRegistrationId(CLIENT_ID))
			.composeCustomizersToOne();
	}
}
