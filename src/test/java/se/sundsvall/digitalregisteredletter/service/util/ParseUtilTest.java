package se.sundsvall.digitalregisteredletter.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

class ParseUtilTest {

	@Test
	void parseLetterRequest_OK() {
		var letterString = """
			{
			  "partyId": "123e4567-e89b-12d3-a456-426614174000",
			  "subject": "Important Notification",
			  "supportInfo": {
			    "supportText": "For support, please contact us at the information below.",
			    "contactInformationUrl": "https://example.com/support",
			    "contactInformationPhoneNumber": "+46123456789",
			    "contactInformationEmail": "support@email.com"
			  },
			  "contentType": "text/plain",
			  "body": "This is the content of the letter. Plain-text body"
			}
			""";

		var object = ParseUtil.parseLetterRequest(letterString);

		assertThat(object).isNotNull();
		assertThat(object.partyId()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
		assertThat(object.subject()).isEqualTo("Important Notification");
		assertThat(object.supportInfo()).isNotNull();
		assertThat(object.supportInfo().supportText()).isEqualTo("For support, please contact us at the information below.");
		assertThat(object.supportInfo().contactInformationUrl()).isEqualTo("https://example.com/support");
		assertThat(object.supportInfo().contactInformationPhoneNumber()).isEqualTo("+46123456789");
		assertThat(object.supportInfo().contactInformationEmail()).isEqualTo("support@email.com");
		assertThat(object.contentType()).isEqualTo("text/plain");
		assertThat(object.body()).isEqualTo("This is the content of the letter. Plain-text body");
	}

	@Test
	void parseLetterRequest_InvalidJson() {
		var letterString = "Invalid JSON string";

		assertThatThrownBy(() -> ParseUtil.parseLetterRequest(letterString))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Couldn't parse letter request");
	}
}
