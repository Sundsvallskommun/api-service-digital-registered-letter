package se.sundsvall.digitalregisteredletter.integration.kivra.configuration;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.digitalregisteredletter.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class KivraPropertiesTest {

	@Autowired
	private KivraProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.baseUrl()).isEqualTo("http://kivra-url.com");
		assertThat(properties.connectTimeout()).isEqualTo(Duration.of(5, SECONDS));
		assertThat(properties.readTimeout()).isEqualTo(Duration.of(15, SECONDS));
		assertThat(properties.oauth2()).isNotNull().satisfies(oauth2 -> {
			assertThat(oauth2.clientId()).isEqualTo("some-client-id");
			assertThat(oauth2.clientSecret()).isEqualTo("some-client-secret");
			assertThat(oauth2.tokenUrl()).isEqualTo("http://token-url.com");
			assertThat(oauth2.authorizationGrantType()).isEqualTo("client_credentials");
		});
	}
}
