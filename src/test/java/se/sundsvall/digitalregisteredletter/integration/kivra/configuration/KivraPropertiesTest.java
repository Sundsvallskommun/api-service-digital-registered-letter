package se.sundsvall.digitalregisteredletter.integration.kivra.configuration;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import org.assertj.core.api.Assertions;
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
		Assertions.assertThat(properties.apiUrl()).isEqualTo("http://kivra-url.com");
		Assertions.assertThat(properties.connectTimeout()).isEqualTo(Duration.of(7, SECONDS));
		Assertions.assertThat(properties.readTimeout()).isEqualTo(Duration.of(8, SECONDS));
		Assertions.assertThat(properties.tenantKey()).isEqualTo("some-tenant-key");
		Assertions.assertThat(properties.oauth2()).isNotNull().satisfies(oauth2 -> {
			Assertions.assertThat(oauth2.clientId()).isEqualTo("some-client-id");
			Assertions.assertThat(oauth2.clientSecret()).isEqualTo("some-client-secret");
			Assertions.assertThat(oauth2.tokenUrl()).isEqualTo("http://token-url.com");
		});
	}
}
